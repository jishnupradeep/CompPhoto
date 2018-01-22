%Loading images (.dng) to Matlab as an array of images
folder = '/Users/jishnupradeep/Documents/MATLAB/high'
filePattern = fullfile(folder, '*.dng');
f=dir(filePattern);
files={f.name};
for k=1:numel(files)
	fullFileName = fullfile(folder, files{k});
	imageStack{k}=imread(fullFileName);
end

%Initializing tables for storing mean, variance and snr at different pixels
mean=zeros(10,1);
variance=zeros(10,1);
snr=zeros(10,1);


%For the pixel at [100,100]
pixel1=zeros(51,2);
sumPixel1=0;
for m=1:numel(files)
    pixel1(m) = im2uint8(imageStack{m}(100,100));
    sumPixel1=sumPixel1+pixel1(m);
end

pixel1(m+1)=sumPixel1/numel(files);
mean(1)= pixel1(m+1);

t=0;
for m=1:numel(files)
    pixel1(m,2)= (pixel1(m)-mean(1))^2;
    t=t+pixel1(m,2);
end
pixel1(m+1,2)=t/numel(files);
variance(1)= pixel1(m+1,2);
snr(1)= (mean(1)/sqrt(variance(1)));





%For pixel at [200,200]
pixel2=zeros(50,1);
sumPixel2=0;
for m=1:numel(files)
    pixel2(m) = im2uint8(imageStack{m}(200,200));
    sumPixel2=sumPixel2+pixel2(m);
end
pixel2(m+1)=sumPixel2/numel(files);
mean(2)= pixel2(m+1);

t=0;
for m=1:numel(files)
    pixel2(m,2)= (pixel2(m)-mean(1))^2;
    t=t+pixel2(m,2);
end
pixel2(m+1,2)=t/numel(files);
variance(2)= pixel2(m+1,2);
snr(2)= (mean(2)/sqrt(variance(2)));




%For pixel at [300,300]
pixel3=zeros(50,1);
sumPixel3=0;
for m=1:numel(files)
    pixel3(m) = im2uint8(imageStack{m}(300,300));
    sumPixel3=sumPixel3+pixel3(m);
end
pixel3(m+1)=sumPixel3/numel(files);
mean(3)= pixel3(m+1);

t=0;
for m=1:numel(files)
    pixel3(m,2)= (pixel3(m)-mean(1))^2;
    t=t+pixel3(m,2);
end
pixel3(m+1,2)=t/numel(files);
variance(3)= pixel3(m+1,2);
snr(3)= (mean(3)/sqrt(variance(3)));



%For pixel at [400,400]
pixel4=zeros(50,1);
sumPixel4=0;
for m=1:numel(files)
    pixel4(m) = im2uint8(imageStack{m}(400,400));
    sumPixel4=sumPixel4+pixel4(m);
end
pixel4(m+1)=sumPixel4/numel(files);
mean(4)= pixel4(m+1);

t=0;
for m=1:numel(files)
    pixel4(m,2)= (pixel4(m)-mean(1))^2;
    t=t+pixel4(m,2);
end
pixel4(m+1,2)=t/numel(files);
variance(4)= pixel4(m+1,2);
snr(4)= (mean(4)/sqrt(variance(4)));


%For pixel at [500,500]
pixel5=zeros(50,1);
sumPixel5=0;
for m=1:numel(files)
    pixel5(m) = im2uint8(imageStack{m}(500,500));
    sumPixel5=sumPixel5+pixel5(m);
end
pixel5(m+1)=sumPixel5/numel(files);
mean(5)= pixel5(m+1);

t=0;
for m=1:numel(files)
    pixel5(m,2)= (pixel5(m)-mean(1))^2;
    t=t+pixel5(m,2);
end
pixel5(m+1,2)=t/numel(files);
variance(5)= pixel5(m+1,2);
snr(5)= (mean(5)/sqrt(variance(5)));


%For pixel at [600,600]
pixel6=zeros(50,1);
sumPixel6=0;
for m=1:numel(files)
    pixel6(1:m) = im2uint8(imageStack{m}(600,600));
    sumPixel6=sumPixel6+pixel6(m);
end
pixel6(m+1)=sumPixel6/numel(files);
mean(6)= pixel6(m+1);

t=0;
for m=1:numel(files)
    pixel6(m,2)= (pixel6(m)-mean(1))^2;
    t=t+pixel6(m,2);
end
pixel6(m+1,2)=t/numel(files);
variance(6)= pixel6(m+1,2);
snr(6)= (mean(6)/sqrt(variance(6)));


%For pixel at [700,700]
pixel7=zeros(50,1);
sumPixel7=0;
for m=1:numel(files)
    pixel7(m) = im2uint8(imageStack{m}(700,700));
    sumPixel7=sumPixel7+pixel7(m);
end
pixel7(m+1)=sumPixel7/numel(files);
mean(7)= pixel7(m+1);

t=0;
for m=1:numel(files)
    pixel7(m,2)= (pixel7(m)-mean(1))^2;
    t=t+pixel7(m,2);
end
pixel7(m+1,2)=t/numel(files);
variance(7)= pixel7(m+1,2);
snr(7)= (mean(7)/sqrt(variance(7)));




%For pixel at [800,800]
pixel8=zeros(50,1);
sumPixel8=0;
for m=1:numel(files)
    pixel8(m) = im2uint8(imageStack{m}(800,800));
    sumPixel8=sumPixel8+pixel8(m);
end
pixel8(m+1)=sumPixel8/numel(files);
mean(8)= pixel8(m+1);

t=0;
for m=1:numel(files)
    pixel8(m,2)= (pixel8(m)-mean(1))^2;
    t=t+pixel8(m,2);
end
pixel8(m+1,2)=t/numel(files);
variance(8)= pixel8(m+1,2);
snr(8)= (mean(8)/sqrt(variance(8)));


%For pixel at [900,900]
pixel9=zeros(50,1);
sumPixel9=0;
for m=1:numel(files)
    pixel9(m) = im2uint8(imageStack{m}(900,900));
    sumPixel9=sumPixel9+pixel9(m);
end
pixel9(m+1)=sumPixel9/numel(files);
mean(9)= pixel9(m+1);

t=0;
for m=1:numel(files)
    pixel9(m,2)= (pixel9(m)-mean(1))^2;
    t=t+pixel9(m,2);
end
pixel9(m+1,2)=t/numel(files);
variance(9)= pixel9(m+1,2);
snr(9)= (mean(9)/sqrt(variance(9)));


%For pixel at [1000,1000]
pixel10=zeros(50,1);
sumPixel10=0;
for m=1:numel(files)
    pixel10(m) = im2uint8(imageStack{m}(1000,1000));
    sumPixel10=sumPixel10+pixel10(m);
end
pixel10(m+1)=sumPixel10/numel(files);
mean(10)= pixel10(m+1);

t=0;
for m=1:numel(files)
    pixel10(m,2)= (pixel10(m)-mean(1))^2;
    t=t+pixel10(m,2);
end
pixel10(m+1,2)=t/numel(files);
variance(10)= pixel10(m+1,2);
snr(10)= (mean(10)/sqrt(variance(10)));


%51st row of each pixelarray stores the mean value of intensity at that pixel. 

%Plot for mean vs variance
plot(mean,variance);

%Polyfit to extarct Gain from the plot
p = polyfit(mean,variance,1);
slope=(p(1));
fprintf("\nGain: %f", p(1));


%To find Noise
pl=polyfit(mean,variance,2);
fprintf("\nRead Noise Variance: %f\n", pl(1));
fprintf("\nRead Noise Variance: %f\n", pl(3));


%To find SNR value
std_dev=sqrt(foo);

snrdb = zeros(10,1);
for i=1:10
    snrdb(i)=10*log(snr(i));
end

%To plot SNR vs Mean
plot(snrdb, mean)

















