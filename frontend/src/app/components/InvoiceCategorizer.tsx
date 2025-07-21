
"use client";

import { useState } from 'react';
import axios from 'axios';
import { TextField, Button, Grid, Typography, Alert, CircularProgress } from '@mui/material';

const InvoiceCategorizer = () => {
    const [description, setDescription] = useState('');
    const [category, setCategory] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [inputError, setInputError] = useState(false);

    const handleSubmit = async () => {
        if (!description.trim()) {
            setInputError(true);
            setError('Description cannot be empty.');
            return;
        }

        setLoading(true);
        setError('');
        setCategory('');
        setInputError(false);

        try {
            const response = await axios.post('http://localhost:8080/api/categorize', description, {
                headers: { 'Content-Type': 'text/plain' }
            });
            setCategory(response.data);
        } catch (err) {
            setError('An error occurred while categorizing the invoice.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Grid container spacing={2} direction="column" alignItems="center" style={{ marginTop: '2rem' }}>
            <Grid item>
                <Typography variant="h4" component="h1" gutterBottom>
                    Invoice Item Categorizer
                </Typography>
            </Grid>
            <Grid item xs={12} sm={8} md={6}>
                <TextField
                    fullWidth
                    label="Invoice Item Description"
                    variant="outlined"
                    value={description}
                    onChange={(e) => {
                        setDescription(e.target.value);
                        if (e.target.value.trim()) {
                            setInputError(false);
                            setError('');
                        }
                    }}
                    error={inputError}
                    helperText={inputError ? "Please enter a description." : ""}
                />
            </Grid>
            <Grid item>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleSubmit}
                    disabled={loading}
                    style={{ position: 'relative' }}
                >
                    {loading && <CircularProgress size={24} style={{ position: 'absolute', top: '50%', left: '50%', marginTop: -12, marginLeft: -12 }} />}
                    Categorize
                </Button>
            </Grid>
            {category && (
                <Grid item>
                    <Alert severity="success">Category: {category}</Alert>
                </Grid>
            )}
            {error && !inputError && (
                <Grid item>
                    <Alert severity="error">{error}</Alert>
                </Grid>
            )}
        </Grid>
    );
};

export default InvoiceCategorizer;
