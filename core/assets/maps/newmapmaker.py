import csv



# These dictionaries do different things depending on tileset

# Dictionary ordering for tileset_dict: Tileset name : [blank tile, top edge tile, bottom edge tile]
# Example: "Dryden" tileset has tree bottoms at top of screen and tree tops at the bottom, 
#          and the "empty space default" tile is, for now, tile 15 which is blank and green

tileset_dict = {
	"drydentile" : [15, 16, 1],
}

# Collision dict: tileset : list of hit collision blocks
collision_dict = {
	"drydentile" : [0,1,2,3,4,10,11,12,13,14,
					25,26,27,28,29,30,31,41,42,43,44,45,46,
					111, 127],
}


# TODO: foreground dict?



map_name =  input("What do you want to call this map?")
map_filename =  input("What do you want to call this map's file?")


map_x = int(input("How long is your map?"))
map_y = int(input("How tall is your map?"))

song = int(input("Which song are you using?"))
tileset = input("What tileset file are you using?")
encounters_type = int(input("What type encounters are you using?"))


with open("game_maps/" + map_filename + ".csv", "w") as map_csv_file:

	writer = csv.writer(map_csv_file, delimiter=",")

	if tileset in tileset_dict:
		blank_tile = tileset_dict[tileset][0]
		top_tile = tileset_dict[tileset][1]
		bot_tile = tileset_dict[tileset][2]

		# will be in collision dict too, obvs
		collision_str_list = [str(x) for x in collision_dict[tileset]]
		collision_str = "/".join(collision_str_list)

	else:
		blank_tile = 0
		top_tile   = 0
		bot_tile   = 0

		collision_str = "/"

	# fill first row w/ top tile
	writer.writerow([top_tile]*map_x)

	# fill middle rows with blank tile
	for _ in range(map_y - 2):
		writer.writerow([blank_tile]*map_x)

	# fill last row w/ bot tile
	writer.writerow([bot_tile]*map_x)


	# As the last row, we type (temporarily) the row that should be put into the map info file
	info_list = ["x:" + map_name, "game_maps/" + map_filename + ".csv",
				 "tilesets/" + tileset + ".png", map_y, map_x, collision_str, song, encounters_type,
				 "/"] 

	writer.writerow(info_list)


print("done!")









































