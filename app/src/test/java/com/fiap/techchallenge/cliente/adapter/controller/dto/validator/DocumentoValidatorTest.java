package com.fiap.techchallenge.cliente.adapter.controller.dto.validator;

import com.fiap.techchallenge.cliente.domain.TipoPessoa;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentoValidatorTest {

    @Test
    void testNormalizarDocumento() {
        assertThat(DocumentoValidator.normalizarDocumento(null)).isNull();
        assertThat(DocumentoValidator.normalizarDocumento("")).isEmpty();
        assertThat(DocumentoValidator.normalizarDocumento("123.456.789-00")).isEqualTo("12345678900");
        assertThat(DocumentoValidator.normalizarDocumento("12.345.678/0001-99")).isEqualTo("12345678000199");
        assertThat(DocumentoValidator.normalizarDocumento("abc.def-ghi")).isEqualTo("ABCDEFGHI");
        assertThat(DocumentoValidator.normalizarDocumento("   12 34  ")).isEqualTo("1234");
    }

    @Test
    void testValidarDocumento_Null() {
        assertThatCode(() -> DocumentoValidator.validarDocumento(null, "123")).doesNotThrowAnyException();
        assertThatCode(() -> DocumentoValidator.validarDocumento(TipoPessoa.PF, null)).doesNotThrowAnyException();
        assertThatCode(() -> DocumentoValidator.validarDocumento(null, null)).doesNotThrowAnyException();
    }

    @Test
    void testValidarDocumento_PF_Valido() {
        assertThatCode(() -> DocumentoValidator.validarDocumento(TipoPessoa.PF, "55736593084")).doesNotThrowAnyException();
    }

    @Test
    void testValidarDocumento_PF_Invalido() {
        assertThatThrownBy(() -> DocumentoValidator.validarDocumento(TipoPessoa.PF, "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CPF inválido");
    }

    @Test
    void testValidarDocumento_PJ_ValidoNumerico() {
        assertThatCode(() -> DocumentoValidator.validarDocumento(TipoPessoa.PJ, "84777801039950")).doesNotThrowAnyException();
    }

    @Test
    void testValidarDocumento_PJ_ValidoAlfanumerico() {
        assertThatCode(() -> DocumentoValidator.validarDocumento(TipoPessoa.PJ, "UE0EGIK9VASF71")).doesNotThrowAnyException();
    }

    @Test
    void testValidarDocumento_PJ_Invalido() {
        assertThatThrownBy(() -> DocumentoValidator.validarDocumento(TipoPessoa.PJ, "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CNPJ inválido");
    }

    @Test
    void testCpfValido_LengthInvalid() {
        assertThat(DocumentoValidator.cpfValido("123")).isFalse();
    }

    @Test
    void testCpfValido_SameDigits() {
        assertThat(DocumentoValidator.cpfValido("11111111111")).isFalse();
    }

    @Test
    void testCpfValido_InvalidFirstDigit() {
        assertThat(DocumentoValidator.cpfValido("55736593094")).isFalse();
    }

    @Test
    void testCpfValido_InvalidSecondDigit() {
        assertThat(DocumentoValidator.cpfValido("55736593085")).isFalse();
    }
    
    @Test
    void testCpfValido_Valid() {
        assertThat(DocumentoValidator.cpfValido("55736593084")).isTrue();
    }

    @Test
    void testCnpjValido_LengthInvalid() {
        assertThat(DocumentoValidator.cnpjValido("123")).isFalse();
    }

    @Test
    void testCnpjValido_SameDigits() {
        assertThat(DocumentoValidator.cnpjValido("11111111111111")).isFalse();
    }

    @Test
    void testCnpjValido_InvalidFirstDigit() {
        assertThat(DocumentoValidator.cnpjValido("84777801039960")).isFalse();
    }

    @Test
    void testCnpjValido_InvalidSecondDigit() {
        assertThat(DocumentoValidator.cnpjValido("84777801039951")).isFalse();
    }

    @Test
    void testCnpjValido_Valid() {
        assertThat(DocumentoValidator.cnpjValido("84777801039950")).isTrue();
    }

    @Test
    void testCnpjAlfanumericoValido_NullOrLengthInvalid() {
        assertThat(DocumentoValidator.cnpjAlfanumericoValido(null)).isFalse();
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("123")).isFalse();
    }

    @Test
    void testCnpjAlfanumericoValido_RegexInvalid() {
        // Last two must be numbers, first 12 can be alphanumeric uppercase
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("UE0EGIK9VASFA1")).isFalse(); // letter at end
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("ue0egik9vasf71")).isFalse(); // lowercase
    }

    @Test
    void testCnpjAlfanumericoValido_SameDigits() {
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("AAAAAABBBBBB11")).isFalse(); 
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("AAAAAAAAAAAA11")).isFalse();
    }

    @Test
    void testCnpjAlfanumericoValido_InvalidFirstDigit() {
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("UE0EGIK9VASF81")).isFalse();
    }

    @Test
    void testCnpjAlfanumericoValido_InvalidSecondDigit() {
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("UE0EGIK9VASF72")).isFalse();
    }

    @Test
    void testCnpjAlfanumericoValido_Valid() {
        assertThat(DocumentoValidator.cnpjAlfanumericoValido("UE0EGIK9VASF71")).isTrue();
    }
}
