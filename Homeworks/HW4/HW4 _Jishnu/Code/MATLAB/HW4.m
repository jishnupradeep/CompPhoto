%Assignment No.4: High Dynamic Range Imaging and Tone-mapping 
%__Jishnu Pradeep__

%Select images to be processed (reads both .jpeg and .png files)
[fNames, fPath] = uigetfile({'*.jpg;*.png'},'MultiSelect','on');

%Load the exposure times to MATLAB
[eFName, eFPath] = uigetfile({'*.txt;'}','MultiSelect','off');

%Aetup exposure times
expTimes = 1 ./ load(fullfile(eFPath, eFName));

%Get filenames of the selected images
size = length(fNames);
files = cell([1 size]);
for i = 1:size
    files(i) = fullfile(fPath, fNames(i));
end

% Load images to MATLAB variable
for i = 1:size
    im = imread(char(files(i)));
    images(:,:,:,i) = im;
end


expT = expTimes;

%Calling function to create a Radiance Map
[radianceMap, rG, gG, bG, rPxVals, gPxVals, bPxVals, rLgExps, gLgExps, bLgExps] = createRadmap(images,expT,0.1);

%Response Curve for Red Channel
figure('Name','Response Curve : Red Channel');
scatter(rLgExps,rPxVals,12,[0.6 0.6 1]); hold on;
plot(rG,1:256,'r','LineWidth',2);
xlabel('log exposure X');
ylabel('pixel value Z');
title('Red Channel')

%Response Curve for Green Channel
figure('Name','Response Curve : Green Channel');
scatter(gLgExps,gPxVals,12,[0.6 0.6 1]); hold on;
plot(gG,1:256,'g','LineWidth',2);
xlabel('log exposure X');
ylabel('pixel value Z');
title('Green Channel')

%Response Curve for Blue Channel
figure('Name','Response Curve : Blue Channel');
scatter(bLgExps,bPxVals,12,[0.6 0.6 1]); hold on;
plot(bG,1:256,'b','LineWidth',2);
xlabel('log exposure X');
ylabel('pixel value Z');
title('Blue Channel')

%Creating a Radiance Map with ColorBar
L = rgb2gray(radianceMap);
figure('Name','Radiance Map');
imagesc(L);
colormap(jet); 
colorbar;

%Normalizing the pixels in the HDR image
radMaptest = radianceMap;
maxValue = max(radMaptest(:));
minValue = min(radMaptest(:));
diff = maxValue-minValue;
test = (radMaptest - minValue)./diff;
figure('Name','Scaled Image');
imshow(test);

%Applying Gamma curve to the image
gamma = 0.3;
figure('Name','Gamma Image');
GammaRGB = test .^ gamma;
imshow(GammaRGB);

%Applying Global Tone Mapping curve to the image
a_value = 0.6;
d = 0.000001; % to correct for log(0)
clear size;
n = (size(radianceMap,1)) * (size(radianceMap,2));
temp = sum(sum( log(d + radianceMap) )) ./ n;
foo = exp(temp);
bar = a_value ./ foo;
image(:,:,1) = bar(1) .* radianceMap(:,:,1);
image(:,:,2) = bar(2) .* radianceMap(:,:,2);
image(:,:,3) = bar(3) .* radianceMap(:,:,3);

%Creating the mapping
image(:,:,1) = image(:,:,1) ./ (1 + image(:,:,1));
image(:,:,2) = image(:,:,2) ./ (1 + image(:,:,2));
image(:,:,3) = image(:,:,3) ./ (1 + image(:,:,3));
figure('Name','Global Tone Mapping');
imshow(image);
