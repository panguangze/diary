import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../models/mood_entry.dart';
import '../providers/app_provider.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  DateTime selectedDate = DateTime.now();
  MoodType? selectedMood;
  String note = '';

  @override
  Widget build(BuildContext context) {
    final appProvider = Provider.of<AppProvider>(context);
    final todayEntry = appProvider.moodEntries
        .firstWhere((element) => element.date == selectedDate.toIso8601String().split('T')[0],
            orElse: () => MoodEntry(date: selectedDate.toIso8601String().split('T')[0], mood: MoodType.normal));

    selectedMood = todayEntry.id != null ? todayEntry.mood : selectedMood;

    return Scaffold(
      appBar: AppBar(
        title: Text('Love Diary'),
        centerTitle: true,
        actions: [
          IconButton(
            icon: Icon(Icons.settings),
            onPressed: () {
              Navigator.pushNamed(context, '/settings');
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Days Together Card
              Card(
                child: Container(
                  width: double.infinity,
                  padding: EdgeInsets.all(20),
                  child: Column(
                    children: [
                      Text(
                        'Days Together',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.w500),
                      ),
                      SizedBox(height: 8),
                      Text(
                        '${appProvider.daysTogether}',
                        style: TextStyle(
                          fontSize: 48,
                          fontWeight: FontWeight.bold,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                      ),
                      SizedBox(height: 8),
                      Text(
                        'with ${appProvider.partnerName}',
                        style: TextStyle(fontSize: 16),
                      ),
                    ],
                  ),
                ),
              ),
              
              SizedBox(height: 20),
              
              // Today's Date
              Text(
                'Today - ${DateFormat('EEEE, MMMM dd, yyyy').format(selectedDate)}',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              
              SizedBox(height: 20),
              
              // Mood Selection
              Text(
                'How are you feeling today?',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w500),
              ),
              
              SizedBox(height: 16),
              
              Wrap(
                spacing: 12,
                runSpacing: 12,
                children: MoodType.values.map((mood) {
                  final isSelected = selectedMood == mood;
                  return GestureDetector(
                    onTap: () {
                      setState(() {
                        selectedMood = mood;
                      });
                      
                      // Save mood entry
                      final moodEntry = MoodEntry(
                        date: selectedDate.toIso8601String().split('T')[0],
                        mood: mood,
                        note: note.isNotEmpty ? note : null,
                      );
                      
                      appProvider.addMoodEntry(moodEntry);
                    },
                    child: AnimatedContainer(
                      duration: Duration(milliseconds: 200),
                      padding: EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                      decoration: BoxDecoration(
                        color: isSelected 
                            ? Theme.of(context).colorScheme.primary 
                            : Theme.of(context).brightness == Brightness.light
                                ? Colors.grey.shade200
                                : Colors.grey.shade800,
                        borderRadius: BorderRadius.circular(24),
                        border: Border.all(
                          color: isSelected 
                              ? Theme.of(context).colorScheme.primary 
                              : Colors.transparent,
                          width: 2,
                        ),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(mood.emoji, style: TextStyle(fontSize: 20)),
                          SizedBox(width: 8),
                          Text(
                            mood.label,
                            style: TextStyle(
                              color: isSelected 
                                  ? Colors.white 
                                  : Theme.of(context).textTheme.bodyMedium?.color,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                }).toList(),
              ),
              
              SizedBox(height: 20),
              
              // Note Input
              TextField(
                decoration: InputDecoration(
                  labelText: 'Add a note (optional)',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  hintText: 'How was your day together?',
                ),
                maxLines: 3,
                onChanged: (value) {
                  note = value;
                  
                  // Update existing entry if mood is already selected
                  if (selectedMood != null) {
                    final moodEntry = MoodEntry(
                      id: todayEntry.id,
                      date: selectedDate.toIso8601String().split('T')[0],
                      mood: selectedMood!,
                      note: value.isNotEmpty ? value : null,
                    );
                    
                    appProvider.addMoodEntry(moodEntry);
                  }
                },
                initialValue: todayEntry.note ?? '',
              ),
              
              SizedBox(height: 20),
              
              // Recent Entries
              Text(
                'Recent Entries',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              
              SizedBox(height: 12),
              
              ...appProvider.moodEntries.take(5).map((entry) {
                return Card(
                  child: ListTile(
                    leading: CircleAvatar(
                      backgroundColor: Theme.of(context).colorScheme.primary.withOpacity(0.2),
                      child: Text(
                        entry.mood.emoji,
                        style: TextStyle(fontSize: 18),
                      ),
                    ),
                    title: Text(
                      DateFormat('MMM dd, yyyy').format(DateTime.parse(entry.date)),
                    ),
                    subtitle: entry.note != null ? Text(entry.note!) : null,
                    trailing: Text(
                      entry.mood.label,
                      style: TextStyle(fontWeight: FontWeight.w500),
                    ),
                  ),
                );
              }).toList(),
            ],
          ),
        ),
      ),
    );
  }
}