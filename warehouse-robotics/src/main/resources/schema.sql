CREATE TABLE IF NOT EXISTS inventory_reservations (
    order_id TEXT PRIMARY KEY,
    item_sku TEXT,
    quantity INTEGER,
    status TEXT
);

CREATE TABLE IF NOT EXISTS robot_tasks (
    task_id TEXT PRIMARY KEY,
    order_id TEXT,
    robot_id TEXT,
    status TEXT
);

CREATE TABLE IF NOT EXISTS dispatch_records (
    dispatch_id TEXT PRIMARY KEY,
    order_id TEXT,
    station_id TEXT,
    status TEXT
);

CREATE TABLE IF NOT EXISTS chain_tracker (
    order_id TEXT PRIMARY KEY,
    step_status TEXT
);