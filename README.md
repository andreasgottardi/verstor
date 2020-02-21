# Versioned file storage project (verstor)

This project offers a versioned file storage where users can store files and get files from every version of each (sub) folder.

## Building

```powershell
.\gradlew.bat publish
```
This command creates a directory "build/repo" with a local artifact. This can be copied to a internal Maven repository or be used in other projects.