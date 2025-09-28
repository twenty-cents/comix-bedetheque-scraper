package com.comix.scrapers.bedetheque.client.scraper;

import com.comix.scrapers.bedetheque.exception.TechnicalException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenericScraperSingletonTest {

    private static MockWebServer mockWebServer;
    private GenericScraperSingleton scraper;

    @BeforeAll
    static void setUpAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        // Reset the singleton instance before each test to ensure isolation,
        // especially for the lastLoading timestamp.
        ReflectionTestUtils.setField(GenericScraperSingleton.class, "instance", null);
        scraper = GenericScraperSingleton.getInstance();
    }

    @Nested
    @DisplayName("Singleton Instance Tests")
    class SingletonInstanceTests {

        @Test
        @DisplayName("getInstance() should always return the same instance")
        void getInstance_shouldAlwaysReturnSameInstance() {
            GenericScraperSingleton instance1 = GenericScraperSingleton.getInstance();
            GenericScraperSingleton instance2 = GenericScraperSingleton.getInstance();
            assertThat(instance1).isSameAs(instance2);
        }

        @Test
        @DisplayName("getInstance() should be thread-safe")
        void getInstance_shouldBeThreadSafe() throws InterruptedException {
            int numberOfThreads = 100;
            ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            final GenericScraperSingleton[] instances = new GenericScraperSingleton[numberOfThreads];

            for (int i = 0; i < numberOfThreads; i++) {
                final int index = i;
                service.submit(() -> {
                    instances[index] = GenericScraperSingleton.getInstance();
                    latch.countDown();
                });
            }

            latch.await(5, TimeUnit.SECONDS);
            service.shutdown();

            for (int i = 1; i < numberOfThreads; i++) {
                assertThat(instances[i]).isSameAs(instances[0]);
            }
        }
    }

    @Nested
    @DisplayName("Document Loading Tests")
    class DocumentLoadingTests {

        @Test
        @DisplayName("load(url) should return a document on success")
        void loadUrl_shouldReturnDocumentOnSuccess() {
            // GIVEN
            String html = "<html><head><title>Test</title></head><body><p>Content</p></body></html>";
            mockWebServer.enqueue(new MockResponse().setBody(html));
            String url = mockWebServer.url("/test-page").toString();

            // WHEN
            Document doc = scraper.load(url, 0);

            // THEN
            assertThat(doc).isNotNull();
            assertThat(doc.title()).isEqualTo("Test");
            assertThat(doc.body().text()).isEqualTo("Content");
        }

        @Test
        @DisplayName("load(url, data) should return a document on success")
        void loadUrlWithData_shouldReturnDocumentOnSuccess() {
            // GIVEN
            String html = "<html><body>Data response</body></html>";
            mockWebServer.enqueue(new MockResponse().setBody(html));
            String url = mockWebServer.url("/search").toString();
            Map<String, String> data = Collections.singletonMap("query", "test");

            // WHEN
            Document doc = scraper.load(url, data, 0);

            // THEN
            assertThat(doc).isNotNull();
            assertThat(doc.body().text()).isEqualTo("Data response");
        }

        @Test
        @DisplayName("load should throw TechnicalException on network error")
        void load_shouldThrowTechnicalExceptionOnNetworkError() {
            // GIVEN
            // We don't enqueue a response, and we connect to a different port to force an IOException
            String invalidUrl = "http://localhost:" + (mockWebServer.getPort() + 1);

            // WHEN & THEN
            assertThatThrownBy(() -> scraper.load(invalidUrl, 0))
                    .isInstanceOf(TechnicalException.class)
                    .hasFieldOrPropertyWithValue("codeMessage", "ERR-SCR-001");
        }
    }

    @Nested
    @DisplayName("Latency Handling Tests")
    class LatencyTests {

        @Test
        @DisplayName("successive loads should respect latency")
        void successiveLoads_shouldRespectLatency() {
            // GIVEN
            long latencyInSeconds = 2;
            mockWebServer.enqueue(new MockResponse().setBody("<html>1</html>"));
            mockWebServer.enqueue(new MockResponse().setBody("<html>2</html>"));
            String url = mockWebServer.url("/latency-test").toString();

            // WHEN
            scraper.load(url, 0); // First call, sets the lastLoading time
            Instant startTime = Instant.now();
            scraper.load(url, latencyInSeconds); // Second call, should wait
            Instant endTime = Instant.now();

            // THEN
            long durationMillis = Duration.between(startTime, endTime).toMillis();
            assertThat(durationMillis).isGreaterThanOrEqualTo(latencyInSeconds * 1000);
        }

        @Test
        @DisplayName("load should throw TechnicalException when sleep is interrupted")
        void load_shouldThrowTechnicalExceptionWhenSleepIsInterrupted() {
            // GIVEN
            long longLatency = 5; // 5 seconds, long enough to be interrupted
            String url = mockWebServer.url("/interrupt").toString();
            mockWebServer.enqueue(new MockResponse().setBody("<html></html>"));

            // This first call sets the lastLoading time
            scraper.load(url, 0);

            // WHEN
            // We start a new thread that will interrupt the main thread after a short delay
            Thread mainThread = Thread.currentThread();
            new Thread(() -> {
                try {
                    Thread.sleep(500); //NOSONAR
                    mainThread.interrupt();
                } catch (InterruptedException e) {
                    // This thread was interrupted, ignore.
                }
            }).start();

            // THEN
            // The second call will trigger the sleep and should be interrupted
            assertThatThrownBy(() -> scraper.load(url, longLatency))
                    .isInstanceOf(TechnicalException.class)
                    .hasFieldOrPropertyWithValue("codeMessage", "ERR-SCR-001")
                    .hasCauseInstanceOf(InterruptedException.class);

            // Clear the interrupted flag for subsequent tests
            Thread.interrupted();
        }
    }
}