# sapp-client
An implementation of a Students App client, designed by Ajinkya Gajbhiye.
Refer: https://uxplanet.org/how-i-redesigned-my-colleges-students-application-for-a-better-user-experience-a-ux-case-study-e0f068c34b3b

High-priority issues:
- Design ID Card
- Add date switching to Category view
- Add Add Task button to Category view
- Add color palette for Category Themes
- Fix image upload for profile picture
- Add home screen vector art
- Implement new Add Task / Task Category selection behavior
- Add people search vector art
- Implement swipe to delete task
- Implement logout
- Implement delete for category
- Implement data saving
- Parallelize initial app load

Functional:
- Login and data caching (with partial update) (LDAP based only, authenticated by a dummy server)
- Mess rendering from server supplied data, updated on hash change.
- Home screen rendering, tasks parsing and management. Displayed as expected.
- Academic timetable, attendance, calendar, etc (as supplied by server).
- People search, events, contacts as supplied by server.
- Theme switching
