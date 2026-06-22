-- ############################################################################
-- FINTRACK MOBILE - SUPABASE INITIAL SCHEMA
-- Description: Complete schema for Fintrack Mobile including tables, RLS,
--              functions, and triggers.
-- ############################################################################

-- 1. TABLES DEFINITION

-- Users table (matches screenshot 1)
-- Linked to Supabase Auth
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    display_name VARCHAR NOT NULL,
    last_name VARCHAR,
    email VARCHAR UNIQUE NOT NULL,
    birth_date VARCHAR,
    currency_code VARCHAR,
    profile_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Supermarkets table (matches screenshot 1)
CREATE TABLE IF NOT EXISTS public.supermarkets (
    id INT4 GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR NOT NULL,
    address VARCHAR,
    schedule VARCHAR,
    rating FLOAT4 DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Offers table (matches screenshot 1)
CREATE TABLE IF NOT EXISTS public.offers (
    id INT4 GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR NOT NULL,
    description TEXT,
    store VARCHAR, -- Can be linked to supermarket name or id
    category VARCHAR,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Remote Purchases table (matches screenshot 2)
CREATE TABLE IF NOT EXISTS public.remote_purchases (
    id INT4 GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    store_name VARCHAR NOT NULL,
    total_amount NUMERIC NOT NULL,
    purchase_date DATE NOT NULL,
    reason VARCHAR,
    products_count INT4 DEFAULT 0,
    synced_at TIMESTAMPTZ DEFAULT NOW(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Remote Products table (matches screenshot 2)
CREATE TABLE IF NOT EXISTS public.remote_products (
    id INT4 GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_id INT4 NOT NULL REFERENCES public.remote_purchases(id) ON DELETE CASCADE,
    name VARCHAR NOT NULL,
    code VARCHAR,
    description TEXT,
    quantity INT4 NOT NULL,
    price_cents INT8 NOT NULL, -- Screenshot shows int8 for price_cents
    discount_cents INT8 DEFAULT 0 -- Screenshot shows int8 for discount_cents
);

-- 2. ROW LEVEL SECURITY (RLS)

-- Enable RLS for all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.supermarkets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.offers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.remote_purchases ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.remote_products ENABLE ROW LEVEL SECURITY;

-- POLICIES FOR 'users'
CREATE POLICY "Users can view their own profile" ON public.users
    FOR SELECT USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile" ON public.users
    FOR UPDATE USING (auth.uid() = id);

-- POLICIES FOR 'supermarkets' (Public Read, Admin Write)
CREATE POLICY "Anyone can view supermarkets" ON public.supermarkets
    FOR SELECT USING (true);

-- POLICIES FOR 'offers' (Public Read, Admin Write)
CREATE POLICY "Anyone can view offers" ON public.offers
    FOR SELECT USING (true);

-- POLICIES FOR 'remote_purchases'
CREATE POLICY "Users can view their own purchases" ON public.remote_purchases
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own purchases" ON public.remote_purchases
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own purchases" ON public.remote_purchases
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own purchases" ON public.remote_purchases
    FOR DELETE USING (auth.uid() = user_id);

-- POLICIES FOR 'remote_products'
-- These rely on the ownership of the parent purchase
CREATE POLICY "Users can view products of their purchases" ON public.remote_products
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM public.remote_purchases
            WHERE public.remote_purchases.id = public.remote_products.purchase_id
            AND public.remote_purchases.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert products into their purchases" ON public.remote_products
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.remote_purchases
            WHERE public.remote_purchases.id = public.remote_products.purchase_id
            AND public.remote_purchases.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can update products of their purchases" ON public.remote_products
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM public.remote_purchases
            WHERE public.remote_purchases.id = public.remote_products.purchase_id
            AND public.remote_purchases.user_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete products of their purchases" ON public.remote_products
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.remote_purchases
            WHERE public.remote_purchases.id = public.remote_products.purchase_id
            AND public.remote_purchases.user_id = auth.uid()
        )
    );

-- 3. FUNCTIONS & TRIGGERS

-- Function to handle new user creation from Supabase Auth
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
  INSERT INTO public.users (id, display_name, email, profile_image_url)
  VALUES (
    NEW.id,
    COALESCE(NEW.raw_user_meta_data->>'display_name', NEW.email),
    NEW.email,
    NEW.raw_user_meta_data->>'avatar_url'
  );
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger for new user
CREATE OR REPLACE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.handle_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for remote_purchases updated_at
CREATE OR REPLACE TRIGGER on_purchase_updated
  BEFORE UPDATE ON public.remote_purchases
  FOR EACH ROW EXECUTE FUNCTION public.handle_updated_at();

-- Function to keep products_count in sync
CREATE OR REPLACE FUNCTION public.sync_products_count()
RETURNS TRIGGER AS $$
BEGIN
  IF (TG_OP = 'INSERT') THEN
    UPDATE public.remote_purchases
    SET products_count = products_count + 1
    WHERE id = NEW.purchase_id;
  ELSIF (TG_OP = 'DELETE') THEN
    UPDATE public.remote_purchases
    SET products_count = products_count - 1
    WHERE id = OLD.purchase_id;
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger for remote_products count
CREATE OR REPLACE TRIGGER on_product_change
  AFTER INSERT OR DELETE ON public.remote_products
  FOR EACH ROW EXECUTE FUNCTION public.sync_products_count();
