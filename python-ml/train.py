import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix
from tensorflow.keras.layers import TextVectorization, Embedding, GlobalAveragePooling1D, Dense, Input
from tensorflow.keras.models import Sequential
import pickle
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np

def load_data(filepath="Datenset.csv"):
    """Loads and preprocesses the dataset."""
    df = pd.read_csv(filepath, sep=';')
    df.columns = ['item', 'category']
    df.dropna(inplace=True) # Remove rows with missing values
    return df

def train_model(df):
    """Trains the model and saves artifacts."""
    # 1. Split Data
    X = df['item']
    y = df['category']
    X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    # 2. Encode Labels
    label_encoder = LabelEncoder()
    y_train_encoded = label_encoder.fit_transform(y_train)
    y_val_encoded = label_encoder.transform(y_val)
    
    # Save the label encoder
    with open('label_encoder.pkl', 'wb') as f:
        pickle.dump(label_encoder, f)

    # 3. Text Vectorization
    vectorize_layer = TextVectorization(
        max_tokens=15000,
        output_mode='int',
        output_sequence_length=60)
    vectorize_layer.adapt(X_train)

    # Save the vocabulary
    with open('vectorize_layer.pkl', 'wb') as f:
        pickle.dump({'config': vectorize_layer.get_config(),
                     'weights': vectorize_layer.get_weights()}, f)

    # 4. Build Model
    num_classes = len(label_encoder.classes_)
    model = Sequential([
        Input(shape=(1,), dtype=tf.string, name="input_layer"),
        vectorize_layer,
        Embedding(
            input_dim=len(vectorize_layer.get_vocabulary()),
            output_dim=128,
            mask_zero=True,
            name="embedding_layer"),
        GlobalAveragePooling1D(name="pooling_layer"),
        Dense(64, activation='relu', name="dense_layer_1"),
        Dense(num_classes, activation='softmax', name="output_layer")
    ])

    # 5. Compile and Train
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )
    
    print("Starting model training...")
    history = model.fit(
        X_train, y_train_encoded,
        epochs=15,
        validation_data=(X_val, y_val_encoded),
        batch_size=32,
        callbacks=[tf.keras.callbacks.EarlyStopping(monitor='val_loss', patience=3, restore_best_weights=True)]
    )
    print("Model training finished.")

    # 6. Save Model
    model.save('invoice_model.keras')
    print("Model saved to 'invoice_model.keras'")
    
    return model, X_val, y_val_encoded, label_encoder

def evaluate_model(model, X_val, y_val_encoded, label_encoder):
    """Evaluates the model and prints a classification report and confusion matrix."""
    print("\n--- Model Evaluation ---")
    
    # Generate predictions
    y_pred_probs = model.predict(X_val)
    y_pred = np.argmax(y_pred_probs, axis=1)

    # Classification Report
    print("\nClassification Report:")
    target_names = [str(cls) for cls in label_encoder.classes_]
    print(classification_report(y_val_encoded, y_pred, target_names=target_names))

    # Confusion Matrix
    print("Generating Confusion Matrix...")
    cm = confusion_matrix(y_val_encoded, y_pred)
    plt.figure(figsize=(12, 10))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', xticklabels=target_names, yticklabels=target_names)
    plt.title('Confusion Matrix')
    plt.ylabel('Actual')
    plt.xlabel('Predicted')
    plt.savefig('confusion_matrix.png')
    print("Confusion matrix saved to 'confusion_matrix.png'")

if __name__ == '__main__':
    data = load_data()
    trained_model, X_validation, y_validation_encoded, le = train_model(data)
    evaluate_model(trained_model, X_validation, y_validation_encoded, le)
