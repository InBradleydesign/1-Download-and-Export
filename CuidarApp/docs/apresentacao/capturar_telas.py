"""Gera capturas de tela do CuidarApp com dados de demonstração.

Uso: sirva a pasta app/src/main/assets/web em http://localhost:8765 e rode
    python3 capturar_telas.py
"""
import json
import random
from datetime import date, timedelta
from pathlib import Path

from playwright.sync_api import sync_playwright

URL = "http://localhost:8765/index.html"
OUT = Path(__file__).parent / "telas"
OUT.mkdir(exist_ok=True)

today = date.today()


def key(d):
    return d.isoformat()


def demo_state():
    settings = {
        "elderlyName": "Maria Aparecida",
        "caregiverName": "Ana Souza",
        "caregiverPhone": "11987654321",
        "emergencyPhone": "192",
        "weightKg": 62,
        "frail": True,
        "waterGoal": 8,
        "deviceOwner": "elderly",
        "fontSize": 100,
        "animationsEnabled": False,
    }
    meds = [
        {"id": 1, "name": "Losartana", "dosage": "1 comprimido de 50 mg", "time": "08:00", "frequency": "daily", "instructions": "Tomar com um copo de água"},
        {"id": 2, "name": "Vitamina D", "dosage": "2.000 UI • 1 cápsula", "time": "08:00", "frequency": "daily", "instructions": "Junto com o café da manhã"},
        {"id": 3, "name": "Metformina", "dosage": "1 comprimido de 850 mg", "time": "12:30", "frequency": "daily", "instructions": "Logo após o almoço"},
        {"id": 4, "name": "Sinvastatina", "dosage": "1 comprimido de 20 mg", "time": "20:00", "frequency": "daily", "instructions": ""},
    ]
    all_days = [0, 1, 2, 3, 4, 5, 6]
    td = (today.weekday() + 1) % 7  # JS getDay()
    exercises = [
        {"id": 11, "name": "Sentar e levantar da cadeira", "type": "strength", "duration": 10, "days": sorted({1, 3, td}), "time": "09:30", "description": "3 séries de 8 a 12 repetições, com os braços cruzados no peito."},
        {"id": 12, "name": "Equilíbrio em um pé só", "type": "balance", "duration": 5, "days": sorted({1, 3, td}), "time": "09:45", "description": "Apoiado na cadeira, 10 segundos em cada perna, 3 vezes."},
        {"id": 13, "name": "Caminhada leve", "type": "gait", "duration": 20, "days": sorted({2, 4, td}), "time": "17:00", "description": "Em ritmo confortável, em local plano e seguro."},
        {"id": 14, "name": "Alongamento de braços e pernas", "type": "flexibility", "duration": 10, "days": sorted({x for x in all_days if x != td} - {1, 3}), "time": "18:00", "description": "Segure cada posição por 20 a 30 segundos."},
    ]
    visits = [
        {"id": 21, "date": key(today + timedelta(days=2)), "time": "10:00", "description": "Consulta com geriatra"},
        {"id": 22, "date": key(today + timedelta(days=4)), "time": "15:00", "description": "Visita da filha Carla"},
        {"id": 23, "date": key(today + timedelta(days=9)), "time": "09:00", "description": "Fisioterapia"},
    ]
    menu = {
        "breakfast": "Pão integral com ovo mexido, mamão e café com leite",
        "lunch": "Arroz, feijão, frango grelhado, salada e legumes",
        "snacks": "Iogurte natural com aveia e banana",
        "dinner": "Sopa de legumes com carne desfiada",
    }
    diet = {str(i): dict(menu) for i in range(7)}

    random.seed(7)
    log = {}
    for back in range(6, 0, -1):
        d = today - timedelta(days=back)
        jsday = (d.weekday() + 1) % 7
        ex_today = [e["id"] for e in exercises if jsday in e["days"]]
        log[key(d)] = {
            "meds": random.sample([1, 2, 3, 4], random.choice([2, 3, 4, 4])),
            "exercises": ex_today if random.random() > 0.55 else [],
            "meals": random.sample(["breakfast", "lunch", "snacks", "dinner"], random.choice([2, 3, 4])),
            "water": random.choice([4, 5, 6, 7, 8]),
            "mood": random.choice([3, 4, 4, 5, 5, 2]),
            "symptoms": [],
            "exerciseFeedback": {},
        }
    log[key(today)] = {"meds": [1, 2], "exercises": [], "meals": ["breakfast"], "water": 3, "mood": 0, "symptoms": [], "exerciseFeedback": {}}

    alerts = [{
        "id": 1, "type": "dizzy", "message": "Sentiu tontura ou dor após \"Caminhada leve\"",
        "date": key(today - timedelta(days=2)), "time": "17:24", "seen": False,
    }]
    data = {"medications": meds, "exercises": exercises, "visits": visits, "diet": diet, "log": log, "alerts": alerts}
    return settings, data


def main():
    settings, data = demo_state()
    errors = []
    with sync_playwright() as p:
        browser = p.chromium.launch(executable_path="/usr/local/bin/google-chrome")
        ctx = browser.new_context(viewport={"width": 390, "height": 844}, device_scale_factor=2,
                                  is_mobile=True, has_touch=True, locale="pt-BR")
        page = ctx.new_page()
        page.on("pageerror", lambda e: errors.append(str(e)))
        page.on("console", lambda m: m.type == "error" and errors.append(m.text))

        page.goto(URL)
        page.evaluate("""([s, d]) => { localStorage.clear();
            localStorage.setItem('cuidarapp_settings', JSON.stringify(s));
            localStorage.setItem('cuidarapp_data', JSON.stringify(d)); }""", [settings, data])
        page.reload()
        page.wait_for_timeout(400)

        def shot(name, full=False):
            page.wait_for_timeout(250)
            page.screenshot(path=str(OUT / f"{name}.png"), full_page=full)

        shot("01-inicio")

        page.click("#btn-enter-elderly")
        shot("02-idoso-hoje")
        page.evaluate("document.getElementById('elderly-content').scrollTop = 600")
        shot("03-idoso-hoje-tarefas")

        page.click("#elderly-nav [data-category=medication]")
        shot("04-idoso-remedios")

        page.click(".category-pill[data-category=exercise]")
        shot("05-idoso-exercicios")
        page.click("[data-task-type=exercise]")
        shot("06-feedback-exercicio")
        page.click("#choice-options .choice-btn >> nth=0")

        page.click("#elderly-nav [data-category=food]")
        page.click("[data-water='1']")
        page.click("[data-water='1']")
        page.wait_for_timeout(2700)
        shot("07-idoso-alimentacao")

        page.click("#elderly-nav [data-category=health]")
        page.click("[data-mood='4']")
        page.click("[data-symptom='Cansaço']")
        page.wait_for_timeout(2700)
        shot("08-idoso-como-estou")

        page.click("#btn-emergency")
        shot("09-emergencia")
        page.click("#screen-emergency [data-back]")
        assert page.is_visible("#screen-elderly"), "voltar da emergência falhou"

        assert page.evaluate("window.CuidarBack()") is True
        assert page.is_visible("#screen-home"), "voltar do Android não levou ao início"

        page.click("#btn-enter-caregiver")
        page.fill("#password-input", "12")
        shot("10-senha-cuidador")
        page.fill("#password-input", "")
        page.type("#password-input", "1234")
        page.wait_for_timeout(300)
        assert page.is_visible("#screen-caregiver"), "senha padrão não funcionou"
        shot("11-cuidador-painel")
        page.evaluate("document.getElementById('caregiver-content').scrollTop = 560")
        shot("12-cuidador-progresso")
        page.evaluate("document.getElementById('caregiver-content').scrollTop = 1020")
        shot("13-cuidador-semana")

        page.click("#btn-add-exercise")
        page.click("[data-preset='2']")
        shot("14-novo-exercicio")
        page.click("#screen-edit [data-back]")

        page.click("#caregiver-nav [data-action=exercise-list]")
        shot("15-lista-exercicios")
        page.click("#screen-edit [data-back]")

        page.click("#btn-edit-diet")
        shot("16-dieta")
        page.click("#screen-edit [data-back]")

        page.click("#btn-caregiver-settings")
        shot("17-configuracoes")
        page.evaluate("document.querySelector('#screen-settings .screen-content').scrollTop = 560")
        shot("18-configuracoes-saude")

        page.click("#settings-elderly-name")
        shot("19-editar-nome")
        page.fill("#input-field", "Maria Aparecida")
        page.click("#input-ok")

        page.click("#toggle-contrast")
        page.click("#btn-font-increase")
        page.click("#btn-font-increase")
        page.click("#btn-font-increase")
        page.wait_for_timeout(2700)
        assert page.evaluate("window.CuidarBack()") is True
        assert page.evaluate("window.CuidarBack()") is True
        page.click("#btn-enter-elderly")
        shot("20-alto-contraste")

        # Fluxo de queda gera alerta para o cuidador
        page.click("#elderly-nav [data-category=health]")
        page.click("[data-fall]")
        page.click("#choice-options .choice-btn >> nth=1")
        alerts = page.evaluate("JSON.parse(localStorage.getItem('cuidarapp_data')).alerts.filter(a => !a.seen).length")
        assert alerts == 2, f"esperava 2 alertas, veio {alerts}"

        browser.close()

    if errors:
        print("ERROS NO CONSOLE:\n" + "\n".join(errors))
        raise SystemExit(1)
    print("OK -", len(list(OUT.glob('*.png'))), "capturas")


if __name__ == "__main__":
    main()
