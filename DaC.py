from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import os
import zipfile
from time import sleep

chrome_options = webdriver.ChromeOptions()

# Diretorio de Download
download_dir = r"C:\Users\bradl\OneDrive\Área de Trabalho\2-Teste de nivelamento Estagio\Processos Divididos\1-Download-and-Export\1-Download-and-Export\Teste-Download"

zip_file = os.path.join(download_dir, 'arquivos_comprimidos.zip')

# Configuração de preferências para download no Chrome
chrome_options.add_experimental_option("prefs", {
    "download.default_directory": download_dir,
    "download.prompt_for_download": False,
    "download.directory_upgrade": True,
    "safebrowsing.enabled": True,
    "plugins.always_open_pdf_externally": True
})

# Função para criar o arquivo ZIP
def criar_zip(arquivos, nome_zip):
    try:
        with zipfile.ZipFile(nome_zip, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for arquivo in arquivos:
                zipf.write(arquivo, os.path.basename(arquivo))  # Adiciona o arquivo ao ZIP com o nome original
        print(f'Arquivo {nome_zip} criado com sucesso.')
    except Exception as e:
        print(f"Erro ao criar o arquivo ZIP: {e}")

# Função para obter os arquivos PDF no diretório de download
def obter_arquivos_download():
    arquivos = os.listdir(download_dir)
    arquivos_pdf = [os.path.join(download_dir, arquivo) for arquivo in arquivos if arquivo.endswith('.pdf')]
    return arquivos_pdf

# Função para verificar o progresso do download
def aguardar_download():
    while True:
        arquivos_baixados = obter_arquivos_download()
        if len(arquivos_baixados) >= 2:  # Verifica se 2 arquivos PDF foram baixados
            return arquivos_baixados
        print(f"Aguardando arquivos: {len(arquivos_baixados)} arquivos baixados.")
        sleep(1)  # Delay para evitar loop rápido

# Função para excluir arquivos PDF no diretório de download
def excluir_arquivos(arquivos):
    for arquivo in arquivos:
        os.remove(arquivo)
    print("Arquivos PDF removidos com sucesso.")

# Função para minimizar a aba "data:" caso ela seja aberta
def minimizar_aba_data(navegador):
    abas = navegador.window_handles
    for aba in abas:
        navegador.switch_to.window(aba)
        if navegador.title.startswith("data:"):
            navegador.minimize_window()
            print("Aba 'data:' minimizada.")
            break

# Iniciar o navegador com as opções configuradas
navegador = webdriver.Chrome(options=chrome_options)

# Minimizar a janela do navegador
navegador.minimize_window()

# Verifica se já existem arquivos PDF no diretório de download
arquivos_baixados = obter_arquivos_download()

if len(arquivos_baixados) >= 2:
    print("Foi detectada a presença de dois PDFs pré-existentes.")
    escolha = input("Deseja remover os arquivos ou mantê-los?\n[1] - Remover\n[2] - Manter\nEscolha: ")

    if escolha == '1':  # Remover arquivos
        excluir_arquivos(arquivos_baixados)
        arquivos_baixados = []  # Resetar a lista para novos downloads
    elif escolha == '2':  # Manter arquivos
        if os.path.exists(zip_file):
            print(f"O arquivo ZIP '{zip_file}' já existe. Processo finalizado.")
            navegador.quit()
            exit()
        else:
            criar_zip(arquivos_baixados, zip_file)
            navegador.quit()
            exit()
    else:
        print("Opção inválida. O processo será finalizado.")
        navegador.quit()
        exit()

# Caso não haja arquivos ou o usuário optou por remover, continuar o download
navegador.get('https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos')

# Aceitação de cookies
try:
    aceito_cookies = WebDriverWait(navegador, 10).until(
        EC.element_to_be_clickable((By.XPATH, '/html/body/div[5]/div/div/div/div/div[2]/button[3]'))
    )
    aceito_cookies.click()
    print("Cookies aceitos.")
except Exception as e:
    print(f"Erro ao tentar aceitar os cookies: {e}")

sleep(2)

# Primeiro arquivo pelo XPath
navegador.find_element(By.XPATH, '//*[@id="cfec435d-6921-461f-b85a-b425bc3cb4a5"]/div/ol/li[1]/a[1]').click()
print("Primeiro arquivo baixando.")

sleep(5)  # Espera inicial para iniciar o download

# Segundo arquivo pelo XPath
try:
    segundo_arquivo = WebDriverWait(navegador, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//*[@id="cfec435d-6921-461f-b85a-b425bc3cb4a5"]/div/ol/li[2]/a'))
    )
    segundo_arquivo.click()
    print("Segundo arquivo baixando.")
except Exception as e:
    print(f"Erro ao tentar baixar o segundo arquivo: {e}")

# Aguarda até que os arquivos sejam baixados
arquivos_baixados = aguardar_download()

# Verifica se os arquivos foram encontrados
if len(arquivos_baixados) >= 2:
    criar_zip(arquivos_baixados[:2], zip_file)
else:
    print("Os arquivos para compressão não foram encontrados.")

# Minimiza a aba "data:" caso tenha sido aberta
minimizar_aba_data(navegador)

# Fecha o navegador
navegador.quit()
