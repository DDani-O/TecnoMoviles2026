-- Script to add ticket_image_url to remote_purchases table
ALTER TABLE public.remote_purchases
ADD COLUMN IF NOT EXISTS ticket_image_url TEXT;
