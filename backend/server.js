/**
 * DKitchen Node.js Express Backend API
 * 
 * Provides production-ready API services for:
 * 1. Database Synchronization: Fetching live home-chef details & gourmet meal lists.
 * 2. Secure Checkout: Generating Stripe PaymentIntent Client Secrets.
 */

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const path = require('path');
// Load environment variables from root .env first, then fallback to backend .env
require('dotenv').config({ path: path.join(__dirname, '..', '.env') });
require('dotenv').config({ path: path.join(__dirname, '.env') });

const app = express();
const PORT = 3000;

// Enable CORS for all incoming client connections (including Android emulator & physical devices)
app.use(cors());
app.use(bodyParser.json());

// Initialize Stripe SDK if Secret Key is provided in environment variables
const stripeSecretKey = process.env.STRIPE_SECRET_KEY;
let stripeInstance = null;

if (stripeSecretKey) {
    stripeInstance = require('stripe')(stripeSecretKey);
    console.log('✓ Stripe API initialized securely with production credentials.');
} else {
    console.warn('⚠️ STRIPE_SECRET_KEY is not defined in your environment/dotenv.');
    console.warn('⚠️ Server will operate in high-fidelity mock payment mode (returning mock client secrets).');
}

// Seeded Chef dataset (matching ChefEntity in Android Room DB)
const chefs = [
    {
        id: 1,
        name: "Chef Elena Rostova",
        rating: 4.9,
        address: "Downtown Kitchen - 124 Pine St",
        cuisineType: "Gourmet Italian & Pastas",
        phone: "+1 (555) 349-2091",
        bio: "Elena studied culinary arts in Florence and specializes in slow-baked organic lasagnas and fresh hand-rolled truffle pastas using locally sourced ingredients.",
        youtubeChannelUrl: "https://www.youtube.com/watch?v=FLeSREbZ7Rk",
        youtubeChannelName: "Elena's Italian Classics",
        avatarUrl: "https://images.unsplash.com/photo-1577219491135-ce391730fb2c?w=150",
        latitude: 37.7812,
        longitude: -122.4111,
        followersCount: 284
    },
    {
        id: 2,
        name: "Chef Kenji Sato",
        rating: 4.8,
        address: "Soma Culinary Loft - 650 Brannan St",
        cuisineType: "Artisanal Ramen & Sushi",
        phone: "+1 (555) 980-1283",
        bio: "Tokyo-trained Ramen professional passionate about delivering authentic rich pork tonkotsu and fresh tori paitan to our local neighborhood.",
        youtubeChannelUrl: "https://www.youtube.com/watch?v=P_mG69_PshQ",
        youtubeChannelName: "Kenji's Ramen Craft",
        avatarUrl: "https://images.unsplash.com/photo-1581092921461-eab62e97a780?w=150",
        latitude: 37.7712,
        longitude: -122.4015,
        followersCount: 390
    },
    {
        id: 3,
        name: "Chef Maya Lin",
        rating: 4.95,
        address: "Pan-Asian Kitchens - 980 Folsom St",
        cuisineType: "Modern Szechuan & Dim Sum",
        phone: "+1 (555) 762-3321",
        bio: "Specializing in fiery, authentic Szechuan hotpots, handcrafted soup dumplings, and hand-pulled noodles. Maya shares street food guides with millions worldwide.",
        youtubeChannelUrl: "https://www.youtube.com/watch?v=uK7_0a_R14s",
        youtubeChannelName: "Maya's Dim Sum Secrets",
        avatarUrl: "https://images.unsplash.com/photo-1595273670150-bd0c3c392e46?w=150",
        latitude: 37.7782,
        longitude: -122.4095,
        followersCount: 1542
    },
    {
        id: 4,
        name: "Chef Marcus Vance",
        rating: 4.75,
        address: "Vance Grill & Smokehouse - 450 Mission St",
        cuisineType: "Texas BBQ & Gourmet Burgers",
        phone: "+1 (555) 459-0012",
        bio: "Passionate smoker pitmaster delivering authentic oakwood-smoked beef briskets, slow-glazed pork ribs, and grass-fed prime burgers with house pickles.",
        youtubeChannelUrl: "https://www.youtube.com/watch?v=Vb_mH3m14v0",
        youtubeChannelName: "Vance Smoked Pitmasters",
        avatarUrl: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
        latitude: 37.7885,
        longitude: -122.3999,
        followersCount: 610
    }
];

// Seeded Meals dataset (matching MealEntity in Android Room DB)
const meals = [
    {
        id: 1,
        chefId: 1,
        name: "Elena's Signature Lasagna",
        description: "Layered with fresh handmade spinach pasta, slow-cooked grass-fed beef ragù, organic creamy bechamel, and melted premium Parmigiano-Reggiano.",
        price: 18.50,
        imageUrl: "https://images.unsplash.com/photo-1574894709920-11b28e7367e3?w=500",
        category: "Pastas",
        isAvailable: true
    },
    {
        id: 2,
        chefId: 1,
        name: "Handmade Truffle Gnocchi",
        description: "Soft potato dumplings pan-seared in an exquisite, aromatic white truffle butter reduction, topped with fresh sage leaves.",
        price: 21.00,
        imageUrl: "https://images.unsplash.com/photo-1621996346565-e3bb64d0be57?w=500",
        category: "Pastas",
        isAvailable: true
    },
    {
        id: 3,
        chefId: 2,
        name: "Black Garlic Tonkotsu Ramen",
        description: "24-hour slow-simmered rich pork marrow broth, thin artisanal wheat noodles, tender rolled chashu pork belly, soft marinated soy egg, and house-infused black garlic oil.",
        price: 17.00,
        imageUrl: "https://images.unsplash.com/photo-1569718212165-3a8278d5f624?w=500",
        category: "Ramen",
        isAvailable: true
    },
    {
        id: 4,
        chefId: 2,
        name: "Chef Kenji's Special Spicy Ramen",
        description: "Our signature rich chicken paitan broth spiced with fermented chili paste, minced spicy pork, roasted nori sheet, and fresh green scallions.",
        price: 16.50,
        imageUrl: "https://images.unsplash.com/photo-1557872943-16a5ac26437e?w=500",
        category: "Ramen",
        isAvailable: true
    },
    {
        id: 5,
        chefId: 3,
        name: "Szechuan Hand-Pulled Biang Biang",
        description: "Freshly pulled thick flat noodles tossed in high-heat aromatic oil, toasted Szechuan peppercorns, roasted peanuts, and garlic vinegar sauce.",
        price: 15.50,
        imageUrl: "https://images.unsplash.com/photo-1585032226651-759b368d7246?w=500",
        category: "Noodles",
        isAvailable: true
    },
    {
        id: 6,
        chefId: 3,
        name: "Hand-Folded Soup Dumplings (Xiao Long Bao)",
        description: "Eight handcrafted delicate wheat wrappers containing minced pork filling and rich savory soup broth, served with fresh julienned ginger and black vinegar.",
        price: 14.00,
        imageUrl: "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=500",
        category: "Dim Sum",
        isAvailable: true
    },
    {
        id: 7,
        chefId: 4,
        name: "Oak-Smoked Texas Beef Brisket",
        description: "14-hour low and slow oak-smoked USDA Prime brisket slice, featuring a perfectly caramelized peppercorn-garlic crust, served with pickled onions.",
        price: 24.50,
        imageUrl: "https://images.unsplash.com/photo-1544025162-d76694265947?w=500",
        category: "BBQ",
        isAvailable: true
    },
    {
        id: 8,
        chefId: 4,
        name: "The Vance Smokehouse Pit Burger",
        description: "Flame-grilled dry-aged beef patty layered with melted smoked cheddar, slow-smoked pulled pork, crispy house onion rings, and Vance signature hickory BBQ sauce.",
        price: 19.00,
        imageUrl: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500",
        category: "Burgers",
        isAvailable: true
    }
];

// Active Orders repository (stored in memory for simulation)
const orders = [];

// Base Health Check endpoint
app.get('/', (req, res) => {
    res.json({
        status: "online",
        service: "DKitchen Live Backend API Server",
        timestamp: new Date().toISOString()
    });
});

// GET /chefs - Returns live chefs list
app.get('/chefs', (req, res) => {
    console.log(`[API] Fetching all chefs list (${chefs.length} items)`);
    res.json(chefs);
});

// GET /meals - Returns live meals list
app.get('/meals', (req, res) => {
    console.log(`[API] Fetching all meals list (${meals.length} items)`);
    res.json(meals);
});

// GET /orders - Retrieve list of placed orders
app.get('/orders', (req, res) => {
    res.json(orders);
});

// POST /payment-intents - Core Stripe PaymentIntent Creation Point
app.post('/payment-intents', async (req, res) => {
    const { amount, description } = req.body;
    
    if (!amount) {
        return res.status(400).json({ error: "Missing required amount field (in cents)." });
    }

    console.log(`[Stripe Checkout] Creating PaymentIntent. Amount: $${(amount / 100).toFixed(2)}, Description: ${description || 'N/A'}`);

    try {
        if (stripeInstance) {
            // Real Stripe API flow
            const paymentIntent = await stripeInstance.paymentIntents.create({
                amount: amount,
                currency: 'usd',
                description: description || 'DKitchen Culinary Checkout',
                automatic_payment_methods: {
                    enabled: true,
                },
            });

            console.log(`[Stripe Success] Created PaymentIntent ID: ${paymentIntent.id}`);
            res.json({
                id: paymentIntent.id,
                client_secret: paymentIntent.client_secret,
                status: paymentIntent.status
            });
        } else {
            // High-fidelity fallback simulated Stripe flow
            const mockId = `pi_mock_${Math.random().toString(36).substring(2, 8).toUpperCase()}`;
            const mockClientSecret = `${mockId}_secret_${Math.random().toString(36).substring(2, 10)}`;
            
            console.log(`[Stripe Simulation] Created mock PaymentIntent ID: ${mockId}`);
            res.json({
                id: mockId,
                client_secret: mockClientSecret,
                status: "requires_payment_method"
            });
        }
    } catch (err) {
        console.error('[Stripe Error]', err);
        res.status(500).json({
            id: null,
            client_secret: null,
            status: "failed",
            error: err.message || "An internal error occurred during payment processing."
        });
    }
});

// Start Express Server
app.listen(PORT, '0.0.0.0', () => {
    console.log(`============================================================`);
    console.log(`🚀 DKitchen Backend API listening on http://localhost:${PORT}`);
    console.log(`🎯 Sync with Android Live Mode by setting API Base URL to: http://<YOUR_IP>:${PORT}`);
    console.log(`============================================================`);
});
