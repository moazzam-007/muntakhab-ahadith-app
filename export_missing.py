import os

files_to_add = [
    'app/src/main/res/values/themes.xml',
    'app/src/main/res/values/colors.xml',
    'app/src/main/res/values/dimens.xml',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/menu/menu_main.xml',
    'app/src/main/res/xml/backup_rules.xml'
]

with open('missing_files.txt', 'w', encoding='utf-8') as out:
    for f in files_to_add:
        out.write(f"\n{'='*80}\nFILE: {f}\n{'='*80}\n\n")
        if os.path.exists(f):
            with open(f, 'r', encoding='utf-8') as file_content:
                out.write(file_content.read())
        else:
            out.write('FILE NOT FOUND\n')
