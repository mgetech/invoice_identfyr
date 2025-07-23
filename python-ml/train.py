import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, precision_recall_curve, average_precision_score
from sklearn.preprocessing import label_binarize
from tensorflow.keras.layers import TextVectorization, Embedding, GlobalAveragePooling1D, Dense, Input
from tensorflow.keras.models import Sequential
import pickle
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np
import os

def load_data(filepath="Datenset.csv"):
    """Loads and preprocesses the dataset."""
    df = pd.read_csv(filepath, sep=';')
    df.columns = ['item', 'category']
    df.dropna(inplace=True)
    return df

def train_model(df):
    """Trains the model and saves artifacts."""
    X = df['item']
    y = df['category']

    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)

    X_train, X_val, y_train_encoded, y_val_encoded = train_test_split(X, y_encoded, test_size=0.2, random_state=42)

    os.makedirs('model', exist_ok=True)

    with open('model/label_encoder.pkl', 'wb') as f:
        pickle.dump(label_encoder, f)

    # Text Vectorization
    vectorize_layer = TextVectorization(
        max_tokens=15000,
        output_mode='int',
        output_sequence_length=60)
    vectorize_layer.adapt(X_train)

    with open('model/vectorize_layer.pkl', 'wb') as f:
        pickle.dump(vectorize_layer.get_vocabulary(), f)

    # Build Model
    num_classes = len(label_encoder.classes_)
    model = Sequential([
        Input(shape=(None,), dtype='int64', name="input_layer"),
        Embedding(
            input_dim=len(vectorize_layer.get_vocabulary()),
            output_dim=128,
            mask_zero=True,
            name="embedding_layer"),
        GlobalAveragePooling1D(name="pooling_layer"),
        Dense(64, activation='relu', name="dense_layer_1"),
        Dense(num_classes, activation='softmax', name="output_layer")
    ])

    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    print("Starting model training...")
    X_train_vectorized = vectorize_layer(tf.constant(X_train.to_numpy(), dtype=tf.string))
    X_val_vectorized = vectorize_layer(tf.constant(X_val.to_numpy(), dtype=tf.string))

    history = model.fit(
        X_train_vectorized, y_train_encoded,
        epochs=15,
        validation_data=(X_val_vectorized, y_val_encoded),
        batch_size=32,
        callbacks=[tf.keras.callbacks.EarlyStopping(monitor='val_loss', patience=3, restore_best_weights=True)]
    )
    print("Model training finished.")

    model.save('model/model.keras')
    print("Model saved to 'invoice_model.keras'")

    return model, X_val, y_val_encoded, label_encoder, vectorize_layer

def evaluate_model(model, X_val, y_val_encoded, label_encoder, vectorize_layer):
    """Evaluates the model and prints a classification report and confusion matrix."""
    print("\n--- Model Evaluation ---")

    # Generate predictions
    X_val_vectorized = vectorize_layer(tf.constant(X_val.to_numpy(), dtype=tf.string))
    y_pred_probs = model.predict(X_val_vectorized)
    y_pred = np.argmax(y_pred_probs, axis=1)

    print("\nClassification Report (incl. macro & weighted averages):")
    unique_labels_val = np.unique(y_val_encoded)
    target_names_subset = [str(c) for c in label_encoder.classes_ if label_encoder.transform([c])[0] in unique_labels_val]

    print(classification_report(
        y_val_encoded,
        y_pred,
        labels=unique_labels_val,
        target_names=target_names_subset,
        digits=3
    ))

    cm = confusion_matrix(y_val_encoded, y_pred, labels=unique_labels_val)

    plt.figure(figsize=(18, 12))
    sns.heatmap(
        cm,
        annot=True,
        fmt='d',
        cmap='Blues',
        xticklabels=target_names_subset,
        yticklabels=target_names_subset,
        annot_kws={"size": 9}
    )
    plt.title('Confusion Matrix', fontsize=16)
    plt.xlabel('Predicted', fontsize=14)
    plt.ylabel('Actual', fontsize=14)
    plt.xticks(rotation=45, ha='right', fontsize=10)
    plt.yticks(fontsize=10)
    plt.tight_layout()
    plt.savefig('confusion_matrix.png')
    plt.show()


    y_val_bin = label_binarize(y_val_encoded, classes=np.arange(len(label_encoder.classes_)))
    ap_scores = {}
    for i in unique_labels_val:
        ap = average_precision_score(y_val_bin[:, i], y_pred_probs[:, i])
        ap_scores[str(label_encoder.classes_[i])] = ap

    print("\nAverage Precision (AUC-PR) per class:")
    for cls, ap in ap_scores.items():
        print(f"  Class {cls:>2}: AP = {ap:.3f}")
    print(f"\nMacro-average AP: {np.mean(list(ap_scores.values())):.3f}")


if __name__ == '__main__':
    data = load_data()
    trained_model, X_validation, y_validation_encoded, le, vectorization_layer = train_model(data)
    evaluate_model(trained_model, X_validation, y_validation_encoded, le, vectorization_layer)