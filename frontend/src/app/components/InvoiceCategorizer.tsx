"use client";

import { useState } from 'react';
import axios from 'axios';
import { TextField, Button, Typography, Alert, CircularProgress, Container, Box } from '@mui/material';

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
            const response = await axios.post('/api/categorize', description, {
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
        <Container maxWidth="sm" sx={{ mt: 4 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
                <Typography variant="h4" component="h1" gutterBottom>
                    Invoice Item Categorizer
                </Typography>
                <TextField
                    fullWidth
                    label="Invoice Item Description"
                    variant="outlined"
                    multiline
                    rows={4}
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
                <Box sx={{ position: 'relative' }}>
                    <Button
                        variant="contained"
                        color="primary"
                        onClick={handleSubmit}
                        disabled={loading}
                    >
                        Categorize
                    </Button>
                    {loading && (
                        <CircularProgress
                            size={24}
                            sx={{
                                color: 'primary.main',
                                position: 'absolute',
                                top: '50%',
                                left: '50%',
                                marginTop: '-12px',
                                marginLeft: '-12px',
                            }}
                        />
                    )}
                </Box>
                {category && (
                    <Alert severity="success" sx={{ width: '100%' }}>Category: {category}</Alert>
                )}
                {error && !inputError && (
                    <Alert severity="error" sx={{ width: '100%' }}>{error}</Alert>
                )}
            </Box>
        </Container>
    );
};

export default InvoiceCategorizer;