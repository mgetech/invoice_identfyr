import pickle
import tensorflow as tf
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import numpy as np
import os

# Define the model directory
MODEL_DIR = 'model'

# Load the vocabulary
with open(os.path.join(MODEL_DIR, 'vectorize_layer.pkl'), 'rb') as f:
    vocabulary = pickle.load(f)

# Create the TextVectorization layer and set its vocabulary
vectorize_layer = tf.keras.layers.TextVectorization(
    max_tokens=len(vocabulary),
    output_mode='int',
    output_sequence_length=60) # Ensure this matches the training configuration
vectorize_layer.set_vocabulary(vocabulary)

# Load the label encoder
with open(os.path.join(MODEL_DIR, 'label_encoder.pkl'), 'rb') as f:
    label_encoder = pickle.load(f)

# Load the trained model
model = tf.keras.models.load_model(os.path.join(MODEL_DIR, 'model.keras'))

app = FastAPI(
    title="Invoice Identfyr ML API",
    description="API for classifying invoice items using a deep learning model.",
    version="1.0.0"
)

class InvoiceItem(BaseModel):
    description: str

class PredictionResponse(BaseModel):
    category: str

@app.post("/predict/", response_model=PredictionResponse)
async def predict_category(item: InvoiceItem):
    """
    Predicts the category of an invoice item.\n\n    - **description**: The invoice item text.\n    """
    if "error" in item.description.lower():
        raise HTTPException(status_code=400, detail="This is a simulated error from FastAPI.")

    # Preprocess the input using the loaded vectorizer
    vectorized_description = vectorize_layer([item.description])

    # Make prediction
    prediction = model.predict(vectorized_description)
    predicted_class_index = np.argmax(prediction, axis=1)

    # Decode the prediction
    predicted_category_name = label_encoder.inverse_transform(predicted_class_index)

    return {"category": str(predicted_category_name[0])}

@app.get("/")
def read_root():
    return {"message": "Welcome to the Invoice Identfyr ML API"}
