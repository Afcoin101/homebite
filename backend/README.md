# 🍳 DKitchen - Production Live Backend API

Welcome to the production-ready **DKitchen Backend API**! This backend handles:
1. **Dynamic Database Synchronization**: Automatically seeds and serves professional home chefs and gourmet menus natively to client applications.
2. **Secure Stripe Checkout**: Integrates with the official Stripe Node SDK to create real payment intents and secure dynamic checkout sessions.

---

## 🛠️ Local Quickstart

### Prerequisite
Make sure you have [Node.js](https://nodejs.org/) installed on your computer.

### 1. Install Dependencies
Navigate into the `backend` directory in your terminal and run:
```bash
npm install
```

### 2. Configure Environment Variables
Copy `.env.example` to a new file named `.env`:
```bash
cp .env.example .env
```
Open `.env` in your editor and enter your **Stripe Secret Key**:
```env
PORT=3000
STRIPE_SECRET_KEY=sk_test_51...your_secret_key...
```

### 3. Start the Server
Run the local dev server:
```bash
npm start
```
You should see:
```text
🚀 DKitchen Backend API listening on http://localhost:3000
✓ Stripe API initialized securely with production credentials.
```

---

## 📱 Connecting the Android Application

To sync your mobile app with this backend:
1. Ensure your computer and Android phone are on the **same Wi-Fi network**.
2. Find your computer's local IP address (e.g., `192.168.1.100` on macOS/Linux via `ifconfig`, or on Windows via `ipconfig`).
3. Open the **DKitchen** app.
4. Navigate to the **Production Go-Live Dashboard** from the bottom menu or side settings.
5. Set the **Base API Endpoint URL** to:
   `http://<YOUR_COMPUTER_IP>:3000`
6. Enter your **Stripe Publishable Token Key** (e.g. `pk_test_...` or `pk_live_...`).
7. Click **Apply Production Keys**.
8. Toggle **Live Production Mode** to **ON**.
9. **Result**: The app will automatically sync its list of home chefs and meals directly from your backend API and process real Stripe payment flows!

---

## ☁️ Deploying to the Cloud

To make your backend globally reachable (so customers can order food from anywhere):

### Option A: Deploy to Render (Free & Fast)
1. Push this folder to a GitHub repository.
2. Sign up on [Render](https://render.com/).
3. Click **New +** -> **Web Service**.
4. Connect your GitHub repository.
5. Configure the service:
   - **Environment**: `Node`
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
6. In the **Environment Variables** tab, add:
   - `STRIPE_SECRET_KEY` = your Stripe secret key.
7. Click **Deploy**. Render will provide a free public URL (e.g., `https://dkitchen-backend.onrender.com`).
8. Paste this URL into your Android App's Go-Live configuration dashboard!

### Option B: Deploy to Google Cloud Run / AWS Elastic Beanstalk
Because this is a standard Express.js app, you can containerize it or run it natively on any serverless hosting platform. Keep `STRIPE_SECRET_KEY` stored securely in the Cloud Provider's Secret Manager.
