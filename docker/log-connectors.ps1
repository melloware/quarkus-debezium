# Define the URL
$Url = "http://localhost:8083/connectors?expand=info&expand=status"

# Fetch the JSON response
$Response = Invoke-RestMethod -Uri $Url -Method Get

# Process the JSON data
$Result = $Response.PSObject.Properties | ForEach-Object {
    $Key = $_.Name
    $Value = $_.Value
    $Type = $Value.info.type
    $State = $Value.status.connector.state
    $TaskStates = $Value.status.tasks | ForEach-Object { $_.state }
    $ConnectorClass = $Value.info.config."connector.class"

    # Combine the extracted values into a formatted string
    "$Type | $Key | $State | $($TaskStates -join ' | ') | $ConnectorClass"
}

# Format the output as a table
$Result | Sort-Object | Format-Table -AutoSize
