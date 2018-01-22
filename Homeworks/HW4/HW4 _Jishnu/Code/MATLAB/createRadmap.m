%Assignment No.4: High Dynamic Range Imaging and Tone-mapping 
%__Jishnu Pradeep__
% This function is to create all response curves from different channels and make the radiance map

function [radianceMap, rG, gG, bG, rPxVals, gPxVals, bPxVals, rLgExps, gLgExps, bLgExps] = createRadmap(imgs,expT,l)

% Load exposure time
B = log(expT);

% Load images
rImgs(:,:,:) = imgs(:,:,1,:);
gImgs(:,:,:) = imgs(:,:,2,:);
bImgs(:,:,:) = imgs(:,:,3,:);

% Sample pixels
rZ = sampling(rImgs);
gZ = sampling(gImgs);
bZ = sampling(bImgs);

%W is initialized as a contstant (given in gsolve.m)
w = ones(256,1)/256;


% Solve for g and lE
[rG, rlE] = gsolve(rZ,B,l);
[gG, glE] = gsolve(gZ,B,l);
[bG, blE] = gsolve(bZ,B,l);

% Creating points to be plot for different channels

%red
rPxVals = zeros(1,numel(rZ));
rLgExps = zeros(1,numel(rZ));
k = 1;
for i=1:size(rZ,1)
    for j=1:size(rZ,2)
        rPxVals(k) = rZ(i,j);
        rLgExps(k) = B(j) + rlE(i);
        k = k+1;
    end
end

%green
gPxVals = zeros(1,numel(gZ));
gLgExps = zeros(1,numel(gZ));
k = 1;
for i=1:size(gZ,1)
    for j=1:size(gZ,2)
        gPxVals(k) = gZ(i,j);
        gLgExps(k) = B(j) + glE(i);
        k = k+1;
    end
end

%blue
bPxVals = zeros(1,numel(bZ));
bLgExps = zeros(1,numel(bZ));
k = 1;
for i=1:size(bZ,1)
    for j=1:size(bZ,2)
        bPxVals(k) = bZ(i,j);
        bLgExps(k) = B(j) + blE(i);
        k = k+1;
    end
end

% Construct radiance map
rE = merge(rImgs,B,rG,w);
gE = merge(gImgs,B,gG,w);
bE = merge(bImgs,B,bG,w);
radianceMap = cat(3,rE,gE,bE);
end