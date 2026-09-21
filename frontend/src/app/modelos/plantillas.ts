/**
 * Codigo inicial por lenguaje. Vive junto al modelo y no dentro de la vista para
 * que agregar un lenguaje en el backend solo obligue a sumar una entrada aqui.
 *
 * En Java la clase debe llamarse Solucion: el motor escribe el codigo en
 * Solucion.java y javac exige que el nombre coincida.
 */
export const PLANTILLAS_VACIAS: Record<string, string> = {
  python: '# Lee de la entrada estandar e imprime el resultado\n\n',

  java: [
    'import java.util.Scanner;',
    '',
    'public class Solucion {',
    '    public static void main(String[] args) {',
    '        Scanner entrada = new Scanner(System.in);',
    '        // escribe tu solucion',
    '    }',
    '}',
    '',
  ].join('\n'),

  javascript: [
    'const entrada = require("fs").readFileSync(0, "utf8");',
    '// escribe tu solucion',
    '',
  ].join('\n'),

  typescript: [
    'import * as fs from "fs";',
    '',
    'const entrada: string = fs.readFileSync(0, "utf8").trim();',
    '// escribe tu solucion',
    '',
  ].join('\n'),

  cobol: [
    'IDENTIFICATION DIVISION.',
    'PROGRAM-ID. SOLUCION.',
    'DATA DIVISION.',
    'WORKING-STORAGE SECTION.',
    '01 N PIC 9(8).',
    '01 R PIC Z(7)9.',
    'PROCEDURE DIVISION.',
    '    ACCEPT N',
    '    *> escribe tu solucion',
    '    STOP RUN.',
    '',
  ].join('\n'),
};

export const PLANTILLAS: Record<string, string> = {
  python: 'valor = int(input())\nprint(valor * 2)\n',

  java: [
    'import java.util.Scanner;',
    '',
    'public class Solucion {',
    '    public static void main(String[] args) {',
    '        Scanner entrada = new Scanner(System.in);',
    '        System.out.println(entrada.nextInt() * 2);',
    '    }',
    '}',
    '',
  ].join('\n'),

  javascript: [
    'const entrada = require("fs").readFileSync(0, "utf8").trim();',
    'console.log(Number(entrada) * 2);',
    '',
  ].join('\n'),

  typescript: [
    'import * as fs from "fs";',
    '',
    'const entrada: string = fs.readFileSync(0, "utf8").trim();',
    'console.log(Number(entrada) * 2);',
    '',
  ].join('\n'),

  cobol: [
    'IDENTIFICATION DIVISION.',
    'PROGRAM-ID. SOLUCION.',
    'DATA DIVISION.',
    'WORKING-STORAGE SECTION.',
    '01 N PIC 9(8).',
    '01 R PIC Z(7)9.',
    'PROCEDURE DIVISION.',
    '    ACCEPT N',
    '    COMPUTE R = N * 2',
    '    DISPLAY FUNCTION TRIM(R)',
    '    STOP RUN.',
    '',
  ].join('\n'),
};
