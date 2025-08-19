# Info

Get icons from IBM Icons...

https://www.ibm.com/design/language/iconography/ui-icons/library

...and alternatively, Phosphor Icons.

https://phosphoricons.com/?q=poly


### Colors

Default color for light mode: #555555

Default colors for dark mode: #aaaaaa


# Glitches

For the implementation to work, the ```svg``` element needs to have attributes ```width``` and ```height```, which tools like Illustrator don't (automatically) do. So you may have to add it manually to the SVGs you add.

Then, a weird bug overrides size 16 into a different size specifically, so it is also needed to modify with or height equal to 16 to something else like 15.999:

```
 width="15.999999" height="15.999999"
```


# Credentials

Ready-made icons by IBM or PhosphorIcons
Custom icons by Côme Broas and Mathieu Jacomy