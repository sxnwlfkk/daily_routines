#! /bin/sh

main_folder="$HOME/heyb0ss/mysrc/daily_routines_android"

index_file="$main_folder/docs/index.p.md"

touch index_file

cat "$main_folder/documentation/index_yaml_header" > $index_file
echo "\n" >> $index_file
cat "$main_folder/documentation/homepage_lead.p.md" >> $index_file
echo "\n" >> $index_file
cat "$main_folder/documentation/guide.p.md" >> $index_file
