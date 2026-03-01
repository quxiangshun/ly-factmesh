# GIT_SEQUENCE_EDITOR: replace "pick 775d431" with "reword 775d431"
param([string]$TodoFile = $args[0])
$c = Get-Content -Path $TodoFile -Raw -Encoding UTF8
$c = $c -replace '^pick 775d431 ', 'reword 775d431 '
[System.IO.File]::WriteAllText($TodoFile, $c, [System.Text.UTF8Encoding]::new($false))
