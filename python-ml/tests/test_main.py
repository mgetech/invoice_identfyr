from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Welcome to the Invoice Identfyr ML API"}

def test_predict_category_success():
    invoice_item = {"description": "Allgemeine Untersuchung mit Beratung, Hund"}
    response = client.post("/predict/", json=invoice_item)
    assert response.status_code == 200
    assert "category" in response.json()
    assert isinstance(response.json()["category"], str)

def test_predict_category_invalid_input():
    invoice_item = {"description": 12345}
    response = client.post("/predict/", json=invoice_item)
    assert response.status_code == 422