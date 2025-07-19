from fastapi.testclient import TestClient
from main import app

client = TestClient(app)

def test_read_root():
    response = client.get("/")
    assert response.status_code == 200
    assert response.json() == {"message": "Welcome to the Invoice Identfyr ML API"}

def test_predict_category_success():
    # This is a sample text. Replace with a more representative one if needed.
    invoice_item = {"description": "Allgemeine Untersuchung mit Beratung, Hund"}
    response = client.post("/predict/", json=invoice_item)
    assert response.status_code == 200
    # The predicted category will depend on the trained model.
    # This assertion checks if the key is present and the value is a string.
    assert "category" in response.json()
    assert isinstance(response.json()["category"], str)

def test_predict_category_invalid_input():
    invoice_item = {"description": 12345}  # Invalid input type
    response = client.post("/predict/", json=invoice_item)
    # FastAPI should return a 422 Unprocessable Entity for validation errors.
    assert response.status_code == 422