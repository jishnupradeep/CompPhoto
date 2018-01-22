%Reading the video file using GUI prompt
[FileName,PathName] = uigetfile('*.mp4','Select the .mp4 video file');
v = VideoReader(FileName);
numFrames = v.NumberOfFrames;

%Extracting frames and converting to grayscale
for i = 1:120
    frames = read(v,i);
    frameArrayg{i}=im2double(rgb2gray(frames));
    frameArrayc{i}=im2double(frames);
end 

fixed_image = frameArrayg{2};
moving_image = frameArrayg{25};

[template, rect] = imcrop(frameArrayg{2});


for i=2:120
correlationOutput = normxcorr2(template, frameArrayg{i});
[maxCorrValue, maxIndex] = max(abs(correlationOutput(:)));
[ypeak, xpeak] = ind2sub(size(correlationOutput),maxIndex(1));
corr_offset = [(xpeak-size(template,2)) (ypeak-size(template,1))];

%figure;
%imshow(frameArrayg{i});
%hold on;
%rectangle('position',[corr_offset(1) corr_offset(2) rect(3) rect(4)],...
%          'edgecolor','g','linewidth',2);

pixel_loc(i-1,1) = (corr_offset(1)+rect(3)/2);
pixel_loc(i-1,2) = (corr_offset(2)+rect(4)/2);
end

%Normalized to start from 0
for i=1:119
    pixel_loc_shift(i,1) = pixel_loc(i,1) - pixel_loc(1,1);
    pixel_loc_shift(i,2) = pixel_loc(i,2) - pixel_loc(1,2);

    pixel_shift(i,1) = pixel_loc(i,1) - pixel_loc(1,1);
    pixel_shift(i,2) = pixel_loc(i,2) - pixel_loc(1,2);
end


%Plotting a Pixel Shift Chart for each frame
n=1;
pt1=[pixel_loc_shift(n,1), pixel_loc_shift(n,2)];
figure;
scatter(pixel_loc_shift(:,1),pixel_loc_shift(:,2));
title('Pixel Shift for each frame of the video');
xlabel('X Pixel Shift'); % x-axis label
ylabel('Y Pixel Shift'); % y-axis label
hold on
for i=1:size(pixel_loc_shift,1)
    if isempty(pt1)~=1
        [Liaa,Locbb] = ismember(pt1(:)',pixel_loc_shift,'rows');
             if Locbb~=0
                pixel_loc_shift(Locbb,:)=[];
                [n,d] = knnsearch(pixel_loc_shift,pt1(:)','k',1);
                x=[pt1(1,1) pixel_loc_shift(n,1)];
                y=[pt1(1,2) pixel_loc_shift(n,2)];
                pt1=[pixel_loc_shift(n,1), pixel_loc_shift(n,2)];
                plot(x,y);
             end
    end
end
hold off


%Using Control Point Selection to find the shift in pixels using 
fixed_image = frameArrayc{2};
moving_image = frameArrayc{25};
cpselect(moving_image,fixed_image);


%Registering images to form synthetic aperture photograph
mytform = fitgeotrans(movingPoints, fixedPoints, 'similarity');
registered = imwarp(moving_image, mytform,'FillValues', 255);
figure, imshow(registered);
figure, imshowpair(fixed_image,registered,'blend');


%Displaying the Synthetic aperture photograph by superimposing other images
Rfixed = imref2d(size(fixed_image));
registered1 = imwarp(moving_image,mytform,'FillValues', 255,'OutputView',Rfixed);
figure, imshowpair(fixed_image,registered1,'blend');

