# ledger

A Clojure app designed to track the daily personal expense list using a text file on your phone or PC and stored in your own Google Drive XL worksheet.

All expense and incomes are by default related to your wallet. Each line in the file relates to a single expense, income and or a balance checkpoint. All of the day's entries are headlined by the `dd-mmm` format. All expenses start with a `-` (minus), all incomes start with a `+`.

Each entry line tracks the following components

* Debit / Credit / Txn amount
* Description
* Tag - Can be used to track accounts (optional)
* Mention (optional)
* Balance (optional)

The grammar for a line are as follows:
```
([+-*]\d+) (\w\s)+ [@\w] [#\w] (bal [\d]+)
```

A sample tracking text file looks like this

```
28-Nov
-340 some expense
-23 some other expense bal 1249
-45 another expense #citiCC

27-nov
+3450 some random income bal 2345
-756 some expense @johndoe
```

## Usage

Need the command to upload the parsed file entries to your Google Doc or output as a CSV.

## License

This project was developed by Poorna Shashank and is licensed under Eclipse Public License either version 1.0 or (at your option) any later version
