Get-ChildItem -Recurse -Filter '*.java' src | ForEach-Object {
    '"' + ($_.FullName -replace '\\', '/') + '"'
} | Out-File -FilePath sources.txt -Encoding ASCII
