# GuiTagger

GuiTagger is program written in Java for tagging data in audio files.
It currently uses [Jaudiotagger](http://www.jthink.net/jaudiotagger/) tagging library. Mp3, Mp4, Ogg Vorbis, Flac and Wma formats are supported. 

## Usage
Either execute GuiTagger.jar or execute the *run* script file to compile and run.

## Info
Currently only a few tag fields are supported. Only the first artwork is displayed and cannot be changed.

## Details
Tagger class is completely independent from the view and is responsible for the logic and the model.
Gui class handles the view.
GuiTaggerException is used for exceptions management.
