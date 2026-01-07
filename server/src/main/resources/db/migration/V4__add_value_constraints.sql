-- Add value range constraints to ensure data validity

-- Payment sum must be positive when specified
ALTER TABLE event 
ADD CONSTRAINT positive_payment_sum 
CHECK (payment_sum IS NULL OR payment_sum > 0);

-- Departure end date must be after start date
ALTER TABLE departure_bid 
ADD CONSTRAINT valid_date_range 
CHECK (day_to > day_from);

