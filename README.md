# Invoice Identfyr: A Full-Stack Invoice Categorization System

This project implements an application designed to categorize invoice items using a deep learning model. It features a responsive web interface, a robust Java backend gateway and a dedicated Python machine learning inference service.

## Architecture

The application follows a three-tiered architecture:

1.  **Frontend (React/Next.js):** A responsive web UI built with Material-UI (MUI) for user interaction.
2.  **Backend Gateway (Java/Spring Boot):** A central API gateway that handles business logic and acts as an intermediary between the frontend and the ML service.
3.  **ML Service (Python/TensorFlow/FastAPI):** A dedicated machine learning inference service responsible for invoice item categorization.

**Data Flow:**
`User Input (React)` -> `HTTP POST (axios)` -> `Java API` -> `HTTP POST (WebClient)` -> `Python API` -> `ML Model Prediction` -> `Response to Java` -> `Response to React` -> `Display Result`


![](https://github.com/mgetech/invoice_identfyr/call-Flow.png)

## Tech Stack

*   **Frontend:** React, Next.js, Material-UI (MUI), Axios
*   **Backend:** Java, Spring Boot, Lombok, WebClient, SLF4J, Springdoc OpenAPI UI
*   **ML Service:** Python, FastAPI, TensorFlow, scikit-learn, pandas
*   **Containerization:** Docker, Docker Compose
*   **Testing:**
    *   **Java:** JUnit 5, Mockito, WireMock
    *   **React:** Jest, React Testing Library, Playwright (E2E)
    *   **Python:** Pytest

## Getting Started

### Running with Docker (Recommended)

The easiest way to get the entire application up and running is by using Docker Compose.

1.  **Build the Docker images:**
    ```bash
    docker-compose build
    ```
2.  **Start the services:**
    ```bash
    docker-compose up -d
    ```
3.  **Access the Frontend:** Open your web browser and navigate to `http://localhost:3000`.

#### Running Tests with Docker

You can also run the tests for each service directly within their Docker containers:

*   **Python ML Service Tests:**
    ```bash
    docker-compose exec python-ml pytest
    ```
*   **Java Backend Tests:**
    ```bash
    docker-compose exec backend ./gradlew test
    ```

### Running Without Docker

If you prefer to run the services individually without Docker, follow these steps:

#### 1. Python ML Service

1.  **Navigate to the `python-ml` directory:**
    ```bash
    cd python-ml
    ```
2.  **Create and activate a virtual environment (recommended):**
    ```bash
    python -m venv venv
    # On Windows:
    .\venv\Scripts\activate
    # On macOS/Linux:
    source venv/bin/activate
    ```
3.  **Install dependencies:**
    ```bash
    pip install -r requirements.txt
    ```
4.  **Run the FastAPI application:**
    ```bash
    uvicorn main:app --host 0.0.0.0 --port 8000
    ```
    The ML service will be available at `http://localhost:8000` and the OpenAPI endpoints at `http://localhost:8000/docs`. 


5.  **Run the Tests:**
    ```bash
    pytest
    ```

#### 2. Java Backend Gateway

1.  **Navigate to the `backend` directory:**
    ```bash
    cd backend
    ```
2.  **Build the Spring Boot application:**
    ```bash
    ./gradlew build
    ```
3.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```
    The backend gateway will be available at `http://localhost:8080` and the OpenAPI endpoints at `http://localhost:8080/swagger-ui/index.html`.


4.  **Run the Tests:**
    ```bash
    ./gradlew test
    ```


#### 3. React Web App

1.  **Navigate to the `frontend` directory:**
    ```bash
    cd frontend
    ```
2.  **Install dependencies:**
    ```bash
    npm install
    ```
3.  **Set the backend URL environment variable:**
    Create a `.env` file in the `frontend` directory with the following content:
    ```
    NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
    ```
4.  **Run the Next.js development server:**
    ```bash
    npm run dev
    ```
5.  **Access the Frontend:** Open your web browser and navigate to `http://localhost:3000`.


6.  **Run the Tests:**     
       ```bash
    # Unit Tests
       npm test
    # End-2-End Tests
       npx playwright test    
       ```