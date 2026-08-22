package com.minidmart.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class PickupSlot {
    private int slotId;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int capacity;
    private int bookedCount;

    public int getSlotId() { return slotId; }
    public void setSlotId(int slotId) { this.slotId = slotId; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }

    public boolean hasCapacity() { return bookedCount < capacity; }
    public int getRemaining() { return capacity - bookedCount; }
}
