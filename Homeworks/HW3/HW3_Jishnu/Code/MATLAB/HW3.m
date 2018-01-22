% Reading the image files and cropping them equally
NoFlash = imread('MATLAB/hw3/noflash.jpg');
Flash = imread('MATLAB/hw3/flash.jpg');
[NoFlash, rect] = imcrop(NoFlash);
Flash = imcrop(Flash, rect);
imwrite(NoFlash,'Images/NoFlash_Original.jpeg');
imwrite(Flash,'Images/Flash_Original.jpeg');



%Converting the images to double
NoFlash = im2double(NoFlash);
Flash = im2double(Flash);



%Separating the channels and applying Bilateral Filter to No_Flash Image. Values selected after testing.
red = NoFlash(:,:,1);
redBF = bilateralFilter(red, 8, 0.05);

green = NoFlash(:,:,2);
greenBF = bilateralFilter(green, 8, 0.05);

blue = NoFlash(:,:,3);
blueBF = bilateralFilter(blue, 8, 0.05);


NoFlashD = cat(3, redBF, greenBF, blueBF);
imwrite(NoFlashD,'Images/No_Flash_Denoised.jpeg');



%Separating the channels and applying Bilateral Filter to Flash Image. Values selected after testing 
red = Flash(:,:,1);
redBF = bilateralFilter(red, 4, 0.08);

green = Flash(:,:,2);
greenBF = bilateralFilter(green, 4, 0.08);

blue = Flash(:,:,3);
blueBF = bilateralFilter(blue, 4, 0.08);


%Forming the image by concatenation and saving it to current directory 
FlashD = cat(3, redBF, greenBF, blueBF);
imwrite(FlashD,'Images/Flash_Denoised.jpeg');


%Fusing the two images using the given function.
Flash = Flash + 0.02;
FlashD = FlashD + 0.02;
temp = Flash./FlashD;
Final = NoFlashD.*temp;


%Saving image files to working directory
imwrite(Final,'Images/Final_image.jpeg');






