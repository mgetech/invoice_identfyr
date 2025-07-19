import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import classification_report, confusion_matrix, precision_recall_curve
from sklearn.preprocessing import label_binarize
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
    # Encode Labels
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)

    X_train, X_val, y_train_encoded, y_val_encoded = train_test_split(X, y_encoded, test_size=0.2, random_state=42)

    
    # Save the label encoder
    with open('model/label_encoder.pkl', 'wb') as f:
        pickle.dump(label_encoder, f)

    # 3. Text Vectorization
    vectorize_layer = TextVectorization(
        max_tokens=15000,
        output_mode='int',
        output_sequence_length=60)
    vectorize_layer.adapt(X_train)

    # Save the vocabulary
    with open('model/vectorize_layer.pkl', 'wb') as f:
        pickle.dump(vectorize_layer.get_vocabulary(), f)

    # Build Model
    num_classes = len(label_encoder.classes_)
    # Define a model that takes integer sequences as input
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

    # Compile and Train
    model.compile(
        optimizer='adam',
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    print("Starting model training...")
    # Vectorize the text data before passing it to the model
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

    # 6. Save Model
    model.save('model/model.keras')
    print("Model saved to 'invoice_model.keras'")
    
    return model, X_val, y_val_encoded, label_encoder

def evaluate_model(model, X_val, y_val_encoded, label_encoder):
    """Evaluates the model and prints a classification report and confusion matrix."""
    print("\n--- Model Evaluation ---")
    
    # Generate predictions
    X_val_vectorized = vectorize_layer(tf.constant(X_val.to_numpy(), dtype=tf.string))
    y_pred_probs = model.predict(X_val_vectorized)
    y_pred = np.argmax(y_pred_probs, axis=1)

    # Classification Report
    print("\nClassification Report:")
    target_names = [str(cls) for cls in label_encoder.classes_]
    unique_labels_val = np.unique(y_val_encoded)
    target_names_subset = [target_names[i] for i in unique_labels_val]

    print(classification_report(y_val_encoded, y_pred, labels=unique_labels_val, target_names=target_names_subset))

    # Confusion Matrix
    print("Generating Confusion Matrix...")
    cm = confusion_matrix(y_val_encoded, y_pred, labels=unique_labels_val)
    plt.figure(figsize=(12, 10))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', xticklabels=target_names_subset, yticklabels=target_names_subset)
    plt.title('Confusion Matrix')
    plt.ylabel('Actual')
    plt.xlabel('Predicted')
    plt.savefig('confusion_matrix.png')
    print("Confusion matrix saved to 'confusion_matrix.png'")

    # Precision-Recall Curve
    print("\nGenerating Precision-Recall Curve...")
    y_val_bin = label_binarize(y_val_encoded, classes=np.arange(len(label_encoder.classes_)))

    # Ensure y_pred_probs has the correct shape for multiclass-multioutput
    if y_pred_probs.shape[1] == 1: # Binary case, expand to 2 classes
        y_pred_probs = np.append(1 - y_pred_probs, y_pred_probs, axis=1)

    precision = dict()
    recall = dict()
    # Iterate only over the unique labels present in the validation set
    for i in unique_labels_val:
        # Find the index corresponding to the unique label in the binarized array
        class_index_in_binarized = np.where(np.arange(len(label_encoder.classes_)) == i)[0][0]
        precision[i], recall[i], _ = precision_recall_curve(y_val_bin[:, class_index_in_binarized], y_pred_probs[:, class_index_in_binarized])
        plt.plot(recall[i], precision[i], lw=2, label=f'class {target_names[i]}')


    plt.xlabel("Recall")
    plt.ylabel("Precision")
    plt.legend(loc="best")
    plt.title("Precision-Recall Curve")
    plt.savefig('precision_recall_curve.png')
    print("Precision-Recall curve saved to 'precision_recall_curve.png'")

if __name__ == '__main__':
    data = load_data()
    trained_model, X_validation, y_validation_encoded, le = train_model(data)
    evaluate_model(trained_model, X_validation, y_validation_encoded, le)
