%Read images into MATLAB as a stack of images. 
folder = '/Users/jishnupradeep/Documents/MATLAB/HW5';
filePattern = fullfile(folder, '*.jpg');
f=dir(filePattern);
files={f.name};
for k=1:numel(files)
	fullFileName = fullfile(folder, files{k});
	imageStack{k}=imread(fullFileName);
end

%Calibrating the focal stack
[fDName, eFPath] = uigetfile({'*.txt;'}','MultiSelect','off');
focalDistance = 1 ./ load(fullfile(eFPath, fDName)); %in Meters
v=focalDistance*1000; %v contains focal distances in mm


%Lens to Sensor Distance 'u'
f = 2.95;
for j=1:numel(files)
    u(j)=(f*v(j))/(v(j)-f);
end

%Magnification factor 'mag'
u=transpose(u);
mean_u=mean(u);
mag = mean_u./u;

%Rescaling all images using the magnification factor
for j=1:numel(files)
    I{j} = mag(j) * imageStack{j};
end

        
%Converting from RGB to greyscale
for j=1:numel(files)
    Igrey{j}=rgb2gray(I{j});
end


%Computing a depth map
l=[1 4 1; 4 -20 4; 1 4 1];
l = (1/6).*l;

for i=1:numel(files)
    I2{i} = imfilter(Igrey{i},l,'conv');
    I2square{i} = abs(I2{i}.^2);
end


x=1944;
y=2592;
kay = 5;
M=I2square;

%Applying Laplacian focus measure
for k=1:numel(files)
    for i = x-kay : x+kay
        for j = y-kay : y+kay
            M{k}(i:j) = M{k}(i:j) + I2square{k}(i:j);
        end
    end
end
       
%Converting into 3D matrix from Array of cells
for k=1:numel(files)
    newMatrix(:,:,k) = M{k}(:,:);
end

%Extracting a depth map
[D,ind] = max(newMatrix,[],3);
figure,imshow(D);



%Selecting pixels based on focus
x=1944;
y=2592;
for i=1:x
    for j=1:y
    allfocusr(i,j) = I{ind(i,j)}(i,j,1);
    allfocusg(i,j) = I{ind(i,j)}(i,j,2);
    allfocusb(i,j) = I{ind(i,j)}(i,j,3);
    end
end

%Concatenating RGB channels to form the final image
finalImage = cat(3, allfocusr, allfocusg, allfocusb);
figure,imshow(finalImage);
