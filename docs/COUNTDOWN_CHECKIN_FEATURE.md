# Countdown Check-in Feature (倒计时打卡功能)

## Overview

This feature implements two types of countdown check-ins as requested:

### 1. Day Countdown (天数倒计时)
- **Purpose**: Automatically count down to a target date
- **Tags**: No tags needed or supported
- **Progress**: Changes automatically with natural calendar days
- **Use Cases**: 
  - Exam countdowns (考试倒计时)
  - Birthday countdowns (生日倒计时)
  - Event countdowns (活动倒计时)

### 2. Check-in Countdown (打卡倒计时)
- **Purpose**: Track progress toward a check-in goal
- **Tags**: Required when creating, used for categorization
- **Progress**: Only changes when user manually checks in
- **Use Cases**:
  - Study plan (学习计划打卡)
  - Exercise routine (运动计划打卡)
  - Habit formation (习惯养成打卡)

## Implementation Details

### Database Schema (Version 11)
Added to `unified_checkin_configs` table:
- `tag`: Optional tag field for check-in countdown
- `countdownMode`: Enum (DAY_COUNTDOWN or CHECKIN_COUNTDOWN)
- `countdownTarget`: Target value (days for day countdown, count for check-in countdown)
- `countdownProgress`: Current progress (used for check-in countdown)

### Core Components

#### Repository Methods
- `createDayCountdown()`: Create a day countdown check-in
- `createCheckInCountdown()`: Create a check-in countdown check-in
- `checkInCountdown()`: Perform a check-in (for check-in countdown only)
- `calculateDaysRemaining()`: Calculate remaining days for day countdown
- `getCheckInCountdownRemaining()`: Get remaining check-ins needed
- `getCountdownProgress()`: Calculate progress percentage (0-100)

#### UI Components
- `AddCountdownDialog`: Dialog for creating new countdown check-ins
  - Mode selection (day countdown vs check-in countdown)
  - Input fields appropriate for selected mode
  - Icon and description customization
- `CountdownCard`: Display card for countdown check-ins
  - Shows remaining days/times
  - Progress bar visualization
  - Check-in button (for check-in countdown only)

#### ViewModel
- `CheckInViewModel`: Manages countdown state and operations
  - `createDayCountdown()`: Create day countdown
  - `createCheckInCountdown()`: Create check-in countdown
  - `checkInCountdown()`: Perform check-in
  - Helper methods for calculations

## Usage

### Creating a Day Countdown
1. Open CheckIn Dashboard
2. Click "+ 添加" in countdown section
3. Enter countdown name
4. Select "天数倒计时" mode
5. Set target date
6. Optional: Add description and select icon
7. Click "确定"

### Creating a Check-in Countdown
1. Open CheckIn Dashboard
2. Click "+ 添加" in countdown section
3. Enter countdown name
4. Select "打卡倒计时" mode
5. Set target number of check-ins
6. Add tag (required for categorization)
7. Optional: Add description and select icon
8. Click "确定"

### Checking In (Check-in Countdown Only)
1. View countdown card on CheckIn Dashboard
2. Click "打卡" button on the card
3. Progress automatically increments
4. Remaining count decreases

## Key Differences

| Feature | Day Countdown | Check-in Countdown |
|---------|--------------|-------------------|
| Progress Update | Automatic (calendar days) | Manual (on check-in) |
| Tags | Not supported | Required |
| User Action | None (view only) | Daily check-in required |
| Target | Target date | Target count |
| Completion | When target date reached | When target count reached |

## Testing

All countdown functionality is covered by unit tests in `CountdownCheckInTest.kt`:
- Day countdown calculations
- Check-in countdown progress
- Progress percentage calculations
- Edge cases (past dates, exceeded targets, etc.)

All 9 tests pass successfully.
