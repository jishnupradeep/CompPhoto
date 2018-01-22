%Assignment No.4: High Dynamic Range Imaging and Tone-mapping 
%__Jishnu Pradeep__
%This function helps in merging images of different color channels into one

%Merging to one color channel
function E = merge(Z,B,g,w)

%Extract image information
height = size(Z,1);
width = size(Z,2);
imgNum = size(Z,3);

%Create radiance map
E = zeros(height,width);
for y=1:height
    for x=1:width
        rSum = 0;
        wSum = 0;
        for j=1:imgNum
            z = Z(y,x,j)+1;
            rSum = rSum+w(z)*(g(z)-B(j));
            wSum = wSum+w(z);
        end
        E(y,x) = exp(rSum/wSum);
    end
end
end