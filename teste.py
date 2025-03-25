from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from time import sleep

chrome_options = webdriver.ChromeOptions()

# Diretorio de Download
download_dir = r"C:\Users\bradl\OneDrive\Área de Trabalho\2-Teste de nivelamento Estagio\Processos Divididos\1-Download-and-Export\1-Download-and-Export\Teste-Download"

chrome_options.add_experimental_option("prefs", {
    "download.default_directory": download_dir,  
    "download.prompt_for_download": False,  
    "plugins.always_open_pdf_externally": True,  # Impede abrir PDFs em uma nova aba
    "safebrowsing.enabled": True  # Habilita a navegação segura
})

# Inicia o navegador com as opções configuradas
navegador = webdriver.Chrome(options=chrome_options)

# Abre a página
navegador.get('https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-da-sociedade/atualizacao-do-rol-de-procedimentos')

# Aceitação de cookies estar visível e clicável
try:
    aceito_cookies = WebDriverWait(navegador, 10).until(
        EC.element_to_be_clickable((By.XPATH, '/html/body/div[5]/div/div/div/div/div[2]/button[3]'))  # Ajuste conforme necessário
    )
    aceito_cookies.click()
    print("Cookies aceitos.")
except Exception as e:
    print(f"Erro ao tentar aceitar os cookies: {e}")

sleep(2)

# Primeiro arquivo pelo XPath e clica nele
navegador.find_element(By.XPATH, '//*[@id="cfec435d-6921-461f-b85a-b425bc3cb4a5"]/div/ol/li[1]/a[1]').click()
print("Primeiro arquivo baixado.")

sleep(5)

# Segundo arquivo pelo XPath ou seletor adequado e clica nele
try:
    segundo_arquivo = WebDriverWait(navegador, 10).until(
        EC.element_to_be_clickable((By.XPATH, '//*[@id="cfec435d-6921-461f-b85a-b425bc3cb4a5"]/div/ol/li[2]/a'))  # Ajuste conforme necessário
    )
    segundo_arquivo.click()
    print("Segundo arquivo baixado.")
except Exception as e:
    print(f"Erro ao tentar baixar o segundo arquivo: {e}")

# Aguarda o download do segundo arquivo
sleep(20)

# Fecha o navegador
navegador.quit()
