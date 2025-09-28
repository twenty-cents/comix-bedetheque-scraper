package com.comix.scrapers.bedetheque.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ApiLanguageHelperTest {

    @InjectMocks
    private ApiLanguageHelper apiLanguageHelper;

    @BeforeEach
    void setUp() {
        // On utilise ReflectionTestUtils pour injecter les valeurs dans les champs privés.
        // Cela simule l'injection via @Value par Spring dans un contexte de test unitaire.
        ReflectionTestUtils.setField(apiLanguageHelper, "acceptedLanguages", List.of("fr", "en", "es"));
        ReflectionTestUtils.setField(apiLanguageHelper, "defaultLanguage", "fr");
    }

    @Nested
    @DisplayName("Tests pour isValidLanguage")
    class IsValidLanguageTests {

        @Test
        @DisplayName("doit retourner true pour une langue supportée")
        void isValidLanguage_withSupportedLanguage_shouldReturnTrue() {
            assertThat(apiLanguageHelper.isValidLanguage("en")).isTrue();
        }

        @Test
        @DisplayName("doit retourner false pour une langue non supportée")
        void isValidLanguage_withUnsupportedLanguage_shouldReturnFalse() {
            assertThat(apiLanguageHelper.isValidLanguage("de")).isFalse();
        }

        @Test
        @DisplayName("doit retourner false pour une langue nulle")
        void isValidLanguage_withNullLanguage_shouldReturnFalse() {
            // La version améliorée avec .contains() est null-safe
            assertThat(apiLanguageHelper.isValidLanguage(null)).isFalse();
        }

        @Test
        @DisplayName("doit retourner false pour une chaîne vide")
        void isValidLanguage_withEmptyString_shouldReturnFalse() {
            assertThat(apiLanguageHelper.isValidLanguage("")).isFalse();
        }
    }

    @Nested
    @DisplayName("Tests pour validateAndApplyApiLanguage")
    class ValidateAndApplyApiLanguageTests {

        @Test
        @DisplayName("doit retourner la même langue si elle est supportée")
        void validateAndApply_withSupportedLanguage_shouldReturnSameLanguage() {
            assertThat(apiLanguageHelper.validateAndApplyApiLanguage("es")).isEqualTo("es");
        }

        @Test
        @DisplayName("doit retourner la langue par défaut si la langue n'est pas supportée")
        void validateAndApply_withUnsupportedLanguage_shouldReturnDefault() {
            assertThat(apiLanguageHelper.validateAndApplyApiLanguage("it")).isEqualTo("fr");
        }

        @Test
        @DisplayName("doit retourner la langue par défaut si la langue est nulle")
        void validateAndApply_withNullLanguage_shouldReturnDefault() {
            assertThat(apiLanguageHelper.validateAndApplyApiLanguage(null)).isEqualTo("fr");
        }

        @Test
        @DisplayName("doit retourner la langue par défaut si la langue est une chaîne vide")
        void validateAndApply_withEmptyString_shouldReturnDefault() {
            assertThat(apiLanguageHelper.validateAndApplyApiLanguage("")).isEqualTo("fr");
        }
    }
}