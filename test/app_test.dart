import 'package:flutter_test/flutter_test.dart';
import 'package:love_diary/models/mood_entry.dart';

void main() {
  group('MoodEntry Model Tests', () {
    test('MoodEntry creation', () {
      final moodEntry = MoodEntry(
        date: '2023-01-01',
        mood: MoodType.happy,
        note: 'A wonderful day!',
      );

      expect(moodEntry.date, '2023-01-01');
      expect(moodEntry.mood, MoodType.happy);
      expect(moodEntry.note, 'A wonderful day!');
    });

    test('MoodEntry toMap and fromMap', () {
      final moodEntry = MoodEntry(
        date: '2023-01-01',
        mood: MoodType.sad,
        note: 'A sad day',
      );

      final map = moodEntry.toMap();
      expect(map['date'], '2023-01-01');
      expect(map['mood'], MoodType.sad.index);
      expect(map['note'], 'A sad day');

      final fromMap = MoodEntry.fromMap(map);
      expect(fromMap.date, moodEntry.date);
      expect(fromMap.mood, moodEntry.mood);
      expect(fromMap.note, moodEntry.note);
    });

    test('MoodType enum values', () {
      expect(MoodType.values.length, 6);
      expect(MoodType.happy.label, 'Happy');
      expect(MoodType.happy.emoji, '😊');
      expect(MoodType.satisfied.label, 'Satisfied');
      expect(MoodType.satisfied.emoji, '🙂');
      expect(MoodType.normal.label, 'Normal');
      expect(MoodType.normal.emoji, '😐');
      expect(MoodType.sad.label, 'Sad');
      expect(MoodType.sad.emoji, '😢');
      expect(MoodType.angry.label, 'Angry');
      expect(MoodType.angry.emoji, '😠');
      expect(MoodType.other.label, 'Other');
      expect(MoodType.other.emoji, '🤔');
    });
  });
}