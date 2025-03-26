import tabula
import pandas as pd
import os

# Caminho do PDF
pdf_path = r"C:\Users\bradl\OneDrive\Área de Trabalho\2-Teste de nivelamento Estagio\Processos Divididos\1-Download-and-Export\1-Download-and-Export\Teste-Download\Anexo_I_Rol_2021RN_465.2021_RN627L.2024.pdf"
csv_output = "Teste_Bradley.csv"

# Tentando extrair tabelas com melhor detecção
tabelas = tabula.read_pdf(pdf_path, pages="all", multiple_tables=True, lattice=True)

# Verifica se alguma tabela foi extraída
if not tabelas:
    print("Nenhuma tabela encontrada no PDF.")
else:
    # Exibir o número de tabelas extraídas
    print(f"Número de tabelas extraídas: {len(tabelas)}")

    # Combinar todas as tabelas extraídas em um único DataFrame
    df_final = pd.concat(tabelas, ignore_index=True)

    # Definir o cabeçalho manualmente
    cabecalho = [
        'DEFAULT', 'PROCEDIMENTO', 'RN (alteração)', 'VIGÊNCIA', 'ODONTOLÓGICA', 'AMBULATORIAL', 'HCO', 'HSO', 'REF', 'PAC', 'DUT', 
        'SUBGRUPO', 'GRUPO', 'CAPÍTULO'
    ]
    
    # Definir o cabeçalho manual no DataFrame
    df_final.columns = cabecalho

    # Excluir a coluna 'DEFAULT' se ela estiver presente
    if 'DEFAULT' in df_final.columns:
        df_final = df_final.drop(columns=['DEFAULT'])

    # Substituir abreviações
    substituicoes = {
        "OD": "Odontológica",
        "AMB": "Ambulatorial"
    }

    df_final.replace(substituicoes, inplace=True)

    # Verifica se o arquivo já existe
    if os.path.exists(csv_output):
        print(f"O arquivo '{csv_output}' já existe.")
        print("[1] - Substituir arquivo")
        print("[2] - Manter arquivo (Encerrar processo)")
        opcao = input("Escolha uma opção: ")

        if opcao == "1":
            os.remove(csv_output)
            print(f"Arquivo existente '{csv_output}' foi removido.")
        elif opcao == "2":
            print("Processo encerrado sem alterações.")
            exit()
        else:
            print("Opção inválida. Processo encerrado.")
            exit()

    # Salvar os dados em CSV corretamente, garantindo que o cabeçalho seja mantido
    df_final.to_csv(csv_output, index=False, sep=";", encoding="utf-8-sig")

    print(f"Arquivo '{csv_output}' gerado com sucesso!")
