# Reading Plans Data Files

This directory contains JSON data files for pre-made Bible reading plans.

## Current Status

**NOTE:** These JSON files currently contain **sample data** with only the first 3 days of readings to demonstrate the structure and format. For production use, these files need to be populated with complete reading schedules for all days.

## Files

1. **bible_in_a_year.json** - 365 days (needs 362 more days)
2. **new_testament_90_days.json** - 90 days (needs 87 more days)
3. **chronological_bible.json** - 365 days (needs 362 more days)
4. **gospels_30_days.json** - 30 days (needs 27 more days)
5. **psalms_proverbs_150_days.json** - 150 days (needs 147 more days)

## JSON Structure

Each plan follows this structure:

```json
{
  "name": "Plan Name",
  "description": "Plan description",
  "durationDays": 365,
  "planType": "canonical|chronological|topical|thematic",
  "difficulty": "easy|medium|hard",
  "tags": ["tag1", "tag2"],
  "readings": [
    {
      "day": 1,
      "references": [
        {
          "bookName": "Genesis",
          "startChapter": 1,
          "startVerse": 1,
          "endChapter": 1,
          "endVerse": 31,
          "displayText": "Genesis 1"
        }
      ]
    }
  ]
}
```

## Completing the Plans

To complete these plans for production:

1. Research or obtain complete reading schedules for each plan type
2. Add all remaining days' readings following the same structure
3. Ensure `durationDays` matches the actual number of reading entries
4. Validate JSON structure before committing

## Testing

The seeding service will validate that:
- All JSON files are valid
- Duration matches number of readings
- All required fields are present
- Day numbers are sequential

Run tests with:
```bash
flutter test test/unit/reading_plans/plan_seeding_test.dart
```
