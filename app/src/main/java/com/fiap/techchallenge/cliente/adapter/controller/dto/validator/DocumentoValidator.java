package com.fiap.techchallenge.cliente.adapter.controller.dto.validator;

import com.fiap.techchallenge.cliente.domain.TipoPessoa;

public class DocumentoValidator {

    // Remove separadores e converte para maiúsculo
    public static String normalizarDocumento(String documento) {
        if (documento == null) return null;
        return documento.replaceAll("[.\\-/\\s]", "").toUpperCase();
    }

    public static void validarDocumento(TipoPessoa tipoPessoa, String documento) {
        if (tipoPessoa == null || documento == null) {
            return;
        }

        if (tipoPessoa == TipoPessoa.PF) {
            String digitos = documento.replaceAll("[^0-9]", "");
            if (!cpfValido(digitos)) {
                throw new IllegalArgumentException("CPF inválido");
            }
        }

        if (tipoPessoa == TipoPessoa.PJ) {
            // Remove separadores, preserva letras (CNPJ alfanumérico)
            String normalizado = documento.replaceAll("[.\\-/\\s]", "").toUpperCase();
            if (!cnpjValido(normalizado) && !cnpjAlfanumericoValido(normalizado)) {
                throw new IllegalArgumentException("CNPJ inválido");
            }
        }
    }

    // Valida CPF pelos dígitos verificadores
    public static boolean cpfValido(String digitos) {
        if (digitos.length() != 11) return false;
        if (digitos.chars().distinct().count() == 1) return false;

        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (digitos.charAt(i) - '0') * (10 - i);
        }
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (digitos.charAt(9) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (digitos.charAt(i) - '0') * (11 - i);
        }
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == (digitos.charAt(10) - '0');
    }

    // Valida CNPJ numérico pelos dígitos verificadores
    public static boolean cnpjValido(String digitos) {
        if (digitos.length() != 14) return false;
        if (digitos.chars().distinct().count() == 1) return false;

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (digitos.charAt(i) - '0') * pesos1[i];
        }
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (digitos.charAt(12) - '0')) return false;

        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (digitos.charAt(i) - '0') * pesos2[i];
        }
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == (digitos.charAt(13) - '0');
    }

    // Valida CNPJ alfanumérico pelos dígitos verificadores
    public static boolean cnpjAlfanumericoValido(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return false;
        if (!cnpj.matches("^[A-Z0-9]{12}[0-9]{2}$")) return false;
        if (cnpj.chars().distinct().count() == 1) return false;

        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += charParaValor(cnpj.charAt(i)) * pesos1[i];
        }
        int resto1 = soma % 11;
        int primeiro = resto1 < 2 ? 0 : 11 - resto1;
        if (primeiro != (cnpj.charAt(12) - '0')) return false;

        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += charParaValor(cnpj.charAt(i)) * pesos2[i];
        }
        int resto2 = soma % 11;
        int segundo = resto2 < 2 ? 0 : 11 - resto2;
        return segundo == (cnpj.charAt(13) - '0');
    }


    // Conversão dos caracteres alfa para seu valor númerico ASCII - 48
    private static int charParaValor(char c) {
        return c - 48;
    }

}
