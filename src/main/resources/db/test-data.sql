-- ============================================================
-- Test Data Seed for Airport Management System
--
-- HOW TO RUN:
--   1. Start the Spring Boot app first (tables are created on startup)
--   2. In IntelliJ: open this file → click "Run" (or right-click → Run)
--
-- TEST CREDENTIALS (password = "password" for all users):
--   admin@airport.com    → ADMIN role    (sees all flights)
--   employee@airport.com → EMPLOYEE role (sees all flights)
--   passenger@airport.com→ PASSENGER role(sees only booked flights: AA100, BA400)
--
-- USEFUL SEARCH QUERIES TO TRY:
--   GET /api/flights                        → all 8 flights (admin/employee)
--   GET /api/flights?status=SCHEDULED       → 2 flights
--   GET /api/flights?routeId=1              → JFK→LHR flights (AA100, AA101, AA102)
--   GET /api/flights?dateFrom=2026-04-10T00:00&dateTo=2026-04-10T23:59 → today's 3 flights
-- ============================================================

-- -------------------------------------------------------
-- 1. ROLES
--    DataInitializer creates these on startup; ON CONFLICT
--    ensures this script is safe to re-run.
-- -------------------------------------------------------
INSERT INTO role (name) VALUES ('PASSENGER') ON CONFLICT (name) DO NOTHING;
INSERT INTO role (name) VALUES ('EMPLOYEE')  ON CONFLICT (name) DO NOTHING;
INSERT INTO role (name) VALUES ('ADMIN')     ON CONFLICT (name) DO NOTHING;

-- -------------------------------------------------------
-- 2. AIRPORTS
-- -------------------------------------------------------
INSERT INTO airport (iata, icao, name, city, country, timezone) VALUES
  ('JFK', 'KJFK', 'John F. Kennedy International', 'New York',  'USA',     'America/New_York'),
  ('LHR', 'EGLL', 'Heathrow Airport',              'London',    'UK',      'Europe/London'),
  ('CDG', 'LFPG', 'Charles de Gaulle Airport',     'Paris',     'France',  'Europe/Paris'),
  ('FRA', 'EDDF', 'Frankfurt Airport',             'Frankfurt', 'Germany', 'Europe/Berlin')
ON CONFLICT (iata) DO NOTHING;

-- -------------------------------------------------------
-- 3. AIRCRAFT
-- -------------------------------------------------------
INSERT INTO aircraft (tail_number, model, capacity, status) VALUES
  ('N737AA', 'Boeing 737',  180, 'ACTIVE'),
  ('N320UA', 'Airbus A320', 150, 'ACTIVE'),
  ('N777BA', 'Boeing 777',  300, 'ACTIVE')
ON CONFLICT (tail_number) DO NOTHING;

-- -------------------------------------------------------
-- 4. ROUTES
--    airport IDs: 1=JFK, 2=LHR, 3=CDG, 4=FRA
-- -------------------------------------------------------
INSERT INTO route (origin_airport_id, dest_airport_id, distance_nm, std_duration_min) VALUES
  (1, 2, 3007, 420),  -- route 1: JFK → LHR
  (2, 3,  184,  80),  -- route 2: LHR → CDG
  (3, 4,  245,  75),  -- route 3: CDG → FRA
  (1, 4, 3856, 480)   -- route 4: JFK → FRA
ON CONFLICT ON CONSTRAINT uk_route_airports DO NOTHING;

-- -------------------------------------------------------
-- 5. FLIGHTS
--    route_id:   1=JFK→LHR, 2=LHR→CDG, 3=CDG→FRA, 4=JFK→FRA
--    aircraft_id: 1=B737,    2=A320,     3=B777
-- -------------------------------------------------------
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'AA100', '2026-04-01 08:00', '2026-04-01 15:00', 'A12', 'LANDED',    1, 1 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'AA100');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'UA200', '2026-04-02 10:00', '2026-04-02 11:20', 'B03', 'LANDED',    2, 2 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'UA200');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'LH300', '2026-04-03 14:00', '2026-04-03 15:15', 'C07', 'CANCELLED', 3, 3 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'LH300');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'AA101', '2026-04-10 09:00', '2026-04-10 16:00', 'A14', 'BOARDING',  1, 1 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'AA101');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'UA201', '2026-04-10 12:00', '2026-04-10 13:20', 'B05', 'DELAYED',   2, 2 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'UA201');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'LH301', '2026-04-10 16:00', '2026-04-10 17:15', 'C09', 'IN_AIR',    3, 3 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'LH301');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'BA400', '2026-04-15 11:00', '2026-04-15 18:20', 'D01', 'SCHEDULED', 3, 4 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'BA400');
INSERT INTO flight (code, departure_scheduled, arrival_scheduled, gate, status, aircraft_id, route_id)
  SELECT 'AA102', '2026-04-20 07:30', '2026-04-20 14:30', 'A16', 'SCHEDULED', 1, 1 WHERE NOT EXISTS (SELECT 1 FROM flight WHERE code = 'AA102');

-- -------------------------------------------------------
-- 6. USERS
--    BCrypt hash below = "password" (cost 10)
--    If login fails, regenerate:
--      new BCryptPasswordEncoder().encode("password")
-- -------------------------------------------------------
INSERT INTO app_user (id, email, phone, password_hash, status, created_at, updated_at) VALUES
  ('a0000000-0000-0000-0000-000000000001',
   'admin@airport.com',     '+1-555-0001',
   '$2a$10$x9kPzcXZe.HdoJSZGvEQbukjXlmDyghQJM2HWz7hi6ppcHNJoxHe2',
   'ACTIVE', NOW(), NOW()),

  ('a0000000-0000-0000-0000-000000000002',
   'employee@airport.com',  '+1-555-0002',
   '$2a$10$x9kPzcXZe.HdoJSZGvEQbukjXlmDyghQJM2HWz7hi6ppcHNJoxHe2',
   'ACTIVE', NOW(), NOW()),

  ('a0000000-0000-0000-0000-000000000003',
   'passenger@airport.com', '+1-555-0003',
   '$2a$10$x9kPzcXZe.HdoJSZGvEQbukjXlmDyghQJM2HWz7hi6ppcHNJoxHe2',
   'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- -------------------------------------------------------
-- 7. USER → ROLE ASSIGNMENTS
--    Role IDs assigned by DataInitializer in enum order:
--      1 = PASSENGER, 2 = EMPLOYEE, 3 = ADMIN
-- -------------------------------------------------------
INSERT INTO user_role (user_id, role_id) VALUES
  ('a0000000-0000-0000-0000-000000000001', 3),  -- admin     → ADMIN
  ('a0000000-0000-0000-0000-000000000002', 2),  -- employee  → EMPLOYEE
  ('a0000000-0000-0000-0000-000000000003', 1)   -- passenger → PASSENGER
ON CONFLICT (user_id, role_id) DO NOTHING;

-- -------------------------------------------------------
-- 8. PASSENGER PROFILE (for passenger user)
-- -------------------------------------------------------
INSERT INTO passenger_profile (user_id, doc_type, doc_number, nationality, loyalty_tier, emergency_contact) VALUES
  ('a0000000-0000-0000-0000-000000000003',
   'PASSPORT', 'US123456789', 'American', 'GOLD', '+1-555-9999')
ON CONFLICT (user_id) DO NOTHING;

-- -------------------------------------------------------
-- 9. BOOKINGS
--    Passenger is booked on:
--      flight 1 (AA100 - LANDED)
--      flight 7 (BA400 - SCHEDULED)
--    When querying as passenger, only these 2 flights appear.
-- -------------------------------------------------------
INSERT INTO booking (passenger_profile_id, flight_id, pnr, status)
  SELECT 'a0000000-0000-0000-0000-000000000003', id, 'PNR-AA100-001', 'CONFIRMED'
  FROM flight WHERE code = 'AA100'
  ON CONFLICT (pnr) DO NOTHING;
INSERT INTO booking (passenger_profile_id, flight_id, pnr, status)
  SELECT 'a0000000-0000-0000-0000-000000000003', id, 'PNR-BA400-001', 'CONFIRMED'
  FROM flight WHERE code = 'BA400'
  ON CONFLICT (pnr) DO NOTHING;
