import os
import glob

def should_include(file_path):
    # Include Java files
    if file_path.endswith('.java'):
        return True
    # Include all XML files in the app
    if file_path.endswith('.xml'):
        return True
    # Include gradle files
    if file_path.endswith('.gradle'):
        return True
    # Include properties and settings
    if file_path.endswith('.properties') or file_path.endswith('settings.gradle'):
        return True
    return False

def main():
    output_file = 'codebase.txt'
    project_root = '.'

    # Find all files recursively
    all_files = []
    for root, dirs, files in os.walk(project_root):
        if '.git' in root or 'build' in root or '.gradle' in root:
            continue
        for file in files:
            file_path = os.path.join(root, file)
            if should_include(file_path):
                all_files.append(file_path)

    with open(output_file, 'w', encoding='utf-8') as out:
        for file_path in all_files:
            out.write(f"\\n{'='*80}\\n")
            out.write(f"FILE: {os.path.relpath(file_path, project_root)}\\n")
            out.write(f"{'='*80}\\n\\n")
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    out.write(f.read())
            except Exception as e:
                out.write(f"Error reading file: {e}\\n")

    print(f"Codebase has been successfully exported to {output_file}")

if __name__ == '__main__':
    main()
