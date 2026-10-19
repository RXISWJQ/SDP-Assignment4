# Warehouse Robotics Fulfillment Facade

## Principle of Least Knowledge (Law of Demeter)

### Before (Without Facade)
Client must know every subsystem, class, and method signature:
```java
OrderBatcher batcher = new OrderBatcher();
batcher.validateBatch("ORD-1", 10);
ShelfInventory inventory = new ShelfInventory(100);
inventory.deduct(10);
InventoryReservation res = new InventoryReservation("ORD-1", "SKU-1", 10, InventoryReservationStatus.NEW);
res.transitionTo(InventoryReservationStatus.RESERVED);
InventoryPersistenceGateway gatewayA = new InventoryPersistenceGateway(dbManager);
gatewayA.save(res);

RobotNavigator nav = new RobotNavigator();
nav.planPath("ROBOT-1", false);
ConveyorController conveyor = new ConveyorController();
conveyor.dispatchConveyor("ORD-1");
RobotTask task = new RobotTask("T-1", "ORD-1", "ROBOT-1", RobotTaskStatus.PENDING);
task.transitionTo(RobotTaskStatus.DISPATCHED);
ProcessingPersistenceGateway gatewayB = new ProcessingPersistenceGateway(dbManager);
gatewayB.save(task);

ChargingStationManager station = new ChargingStationManager();
station.allocateStation("ST-1", false);
DispatchRecord record = new DispatchRecord("D-1", "ORD-1", "ST-1", DispatchRecordStatus.PENDING);
record.transitionTo(DispatchRecordStatus.CONFIRMED);
FinalizationPersistenceGateway gatewayC = new FinalizationPersistenceGateway(dbManager);
gatewayC.save(record);