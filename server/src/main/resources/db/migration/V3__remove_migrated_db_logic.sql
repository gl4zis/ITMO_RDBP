-- Drop all triggers (order matters for dependencies)
DROP TRIGGER IF EXISTS validate_rcb_trigger ON room_change_bid;
DROP TRIGGER IF EXISTS check_room_occupancy_trigger ON resident;
DROP TRIGGER IF EXISTS room_change_auto_deny_trigger ON room_change_bid;
DROP TRIGGER IF EXISTS create_new_bid_notifications_trigger ON bid;
DROP TRIGGER IF EXISTS create_processed_bid_notification_trigger ON bid;

-- Drop all functions (order matters for dependencies)
DROP FUNCTION IF EXISTS validate_rcb();
DROP FUNCTION IF EXISTS check_room_occupancy();
DROP FUNCTION IF EXISTS room_change_auto_deny();
DROP FUNCTION IF EXISTS create_new_bid_notifications();
DROP FUNCTION IF EXISTS create_processed_bid_notification();
DROP FUNCTION IF EXISTS get_last_payment_time(varchar);
DROP FUNCTION IF EXISTS calculate_resident_debt(varchar);
DROP FUNCTION IF EXISTS get_residents_to_eviction_by_debt();
DROP FUNCTION IF EXISTS is_room_filled(int);