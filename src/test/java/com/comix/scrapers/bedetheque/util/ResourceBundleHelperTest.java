package com.comix.scrapers.bedetheque.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceBundleHelperTest {

    @Nested
    @DisplayName("Tests pour getLocalizedMessage(key, locale)")
    class GetLocalizedMessageSimple {

        @Test
        @DisplayName("doit retourner le message correct pour une Locale donnée (en)")
        void shouldReturnEnglishMessage() {
            String message = ResourceBundleHelper.getLocalizedMessage("test.simple", Locale.ENGLISH);
            assertThat(message).isEqualTo("Hello World");
        }

        @Test
        @DisplayName("doit retourner le message correct pour une Locale donnée (fr)")
        void shouldReturnFrenchMessage() {
            String message = ResourceBundleHelper.getLocalizedMessage("test.simple", Locale.FRENCH);
            assertThat(message).isEqualTo("Bonjour le Monde");
        }

        @Test
        @DisplayName("doit retourner le message correct pour une chaîne de langue")
        void shouldReturnMessageForLanguageTag() {
            String message = ResourceBundleHelper.getLocalizedMessage("test.simple", "fr");
            assertThat(message).isEqualTo("Bonjour le Monde");
        }

        @Test
        @DisplayName("doit lever MissingResourceException pour une clé inexistante")
        void shouldThrowExceptionForMissingKey() {
            assertThatThrownBy(() -> ResourceBundleHelper.getLocalizedMessage("non.existent.key", Locale.ENGLISH))
                    .isInstanceOf(MissingResourceException.class)
                    .hasMessageContaining("Can't find resource for bundle java.util.PropertyResourceBundle, key non.existent.key");
        }
    }

    @Nested
    @DisplayName("Tests pour getLocalizedMessage(key, args, locale)")
    class GetLocalizedMessageWithArgs {

        @Test
        @DisplayName("doit retourner le message correctement formaté avec un argument (en)")
        void shouldReturnFormattedEnglishMessage() {
            Object[] args = {"Vincent"};
            String message = ResourceBundleHelper.getLocalizedMessage("test.formatted", args, Locale.ENGLISH);
            assertThat(message).isEqualTo("Hello Vincent, welcome to the test!");
        }

        @Test
        @DisplayName("doit retourner le message correctement formaté avec plusieurs arguments (fr)")
        void shouldReturnFormattedFrenchMessageWithMultipleArgs() {
            Object[] args = {"Alice", 100};
            String message = ResourceBundleHelper.getLocalizedMessage("test.multi.args", args, Locale.FRENCH);
            assertThat(message).isEqualTo("L'utilisateur Alice a 100 points.");
        }

        @Test
        @DisplayName("doit retourner le message non formaté si le tableau d'arguments est null")
        void shouldReturnUnformattedMessageForNullArgs() {
            String message = ResourceBundleHelper.getLocalizedMessage("test.formatted", null, Locale.ENGLISH);
            assertThat(message).isEqualTo("Hello {0}, welcome to the test!");
        }

        @Test
        @DisplayName("doit retourner le message non formaté si le tableau d'arguments est vide")
        void shouldReturnUnformattedMessageForEmptyArgs() {
            String message = ResourceBundleHelper.getLocalizedMessage("test.formatted", new Object[]{}, Locale.ENGLISH);
            assertThat(message).isEqualTo("Hello {0}, welcome to the test!");
        }

        @Test
        @DisplayName("doit retourner le message correctement formaté avec une chaîne de langue")
        void shouldReturnFormattedMessageForLanguageTag() {
            Object[] args = {"Bob"};
            String message = ResourceBundleHelper.getLocalizedMessage("test.formatted", args, "fr");
            assertThat(message).isEqualTo("Bonjour Bob, bienvenue au test !");
        }
    }

    @Nested
    @DisplayName("Tests pour getLocalizedMessage(key, args) utilisant la locale de session")
    class GetLocalizedMessageWithSessionLocale {

        @Test
        @DisplayName("doit toujours retourner le message formaté en anglais")
        void shouldAlwaysReturnEnglishMessage() {
            // Cette méthode utilise la méthode privée getSessionLocale() qui retourne "en" en dur.
            Object[] args = {"SessionUser"};
            String message = ResourceBundleHelper.getLocalizedMessage("test.formatted", args);
            assertThat(message).isEqualTo("Hello SessionUser, welcome to the test!");
        }
    }
}