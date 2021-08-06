# Benno4j

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/e1c293c28dc44e639d747e447899d6a6)](https://www.codacy.com/gh/stwe/Benno4j/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=stwe/Benno4j&amp;utm_campaign=Badge_Grade)
[![License: GPL v2](https://img.shields.io/badge/license-GPL--2.0-green)](https://github.com/stwe/Benno4j/blob/master/LICENSE)


<img src="https://github.com/stwe/Benno4j/blob/master/devLog/31-07-2021.png" width="250" height="200" />

## Supported versions

The project works with Anno 1602:

- NINA ("Neue Inseln, Neue Abenteuer")
- History Edition

## Build instructions

The project comes with the `SgOgl4j` jar file for windows and linux. 
This library is under the "lib" folder and is needed to build. Right click on it and select "Add as library".


The file `benno.properties` must be edited. Enter the correct path to the savegames and the game there.

## Run instructions

This project uses OpenGL to render all the content without any abstraction layer like SDL.
The contents of the `stadtfld.bsh` files are sent to the GPU, but they must first be combined to Tile Atlas textures.
So in the best case (SGFX) instead of 5964 images, we only have two textures. The program can create the TileAtlas images.
To do this, set the `CREATE_ATLAS_IMAGES` option to `true` in the `benno.properties`. Then the files must be copied to `resources/atlas`.
Then set the option back to `false`. Rebuild and restart the program.

## License

Benno4j is licensed under the GPL-2.0 License, see [LICENSE](https://github.com/stwe/Benno4j/blob/master/LICENSE) for more information.
