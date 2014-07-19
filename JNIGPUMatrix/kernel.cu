#include <thrust/host_vector.h>
#include <thrust/device_vector.h>
#include <thrust/generate.h>
#include <thrust/sort.h>
#include <thrust/copy.h>
#include <thrust/reduce.h>
#include <algorithm>
#include <cstdlib>
#include <cuda.h>
#include <cuda_runtime.h>

#include <helper_cuda.h>
#include <stdio.h>

#define N_THREADS_X 8
#define N_THREADS_Y 8

__global__ void K_CheckNonZerosInCol(float *raw, int rows, int cols, int *nonZeros){
	
	int idx = blockDim.x * blockIdx.x + threadIdx.x;
	
	if (idx < rows){
		
		for (int i = 0; i < cols; i++){
			if (raw[idx * cols + i] > 0){
				nonZeros[idx] = 1;
			}
		}
	}
	
}

__global__ void K_Mean(float* mat_sum_depths, float *out_mean, int rows, int cols, float divide_by){
	
	float sum_depths  = 0;

	int columIdx = blockIdx.x * blockDim.x + threadIdx.x;

	if (columIdx < cols){
		
		
		for (int i = 0; i < rows; i++){
			sum_depths += mat_sum_depths[columIdx + (i * cols)];
		}
		out_mean[columIdx] = sum_depths / divide_by ;
	}
}

__global__ void K_Variance(float* layer, float* mean, int rows, int cols){

	int idx = blockIdx.x * blockDim.x + threadIdx.x;
	int idy = blockIdx.y * blockDim.y + threadIdx.y;

	if ((idx < cols) && (idy < rows)){
		float meanv = mean[idx];
		float value = meanv - layer[idy * cols + idx];

		layer[idy * cols + idx] = value * value;
	}
}

__global__ void K_StandardDeviation(float* mat_sum_depths, float *out_sd, int rows, int cols, int depths){

	float sum_depths  = 0;

	int columIdx = blockIdx.x * blockDim.x + threadIdx.x;

	if (columIdx < cols){
		for (int i = 0; i < rows; i++){
			sum_depths += mat_sum_depths[columIdx + (i * cols)];
		}
		out_sd[columIdx] = sqrt(sum_depths / (rows  * depths));
	}
}

__global__ void AddKernel(float* _mat1, float *_mat2, float *_res, 
	int rows1, int cols, int cols2){
    
    int idX = blockDim.x * blockIdx.x + threadIdx.x;
    int idY = blockDim.y * blockIdx.y + threadIdx.y;
     
    
    int id = idY * cols2 + idX;
    
    if (id < rows1 * cols2){
    
    	_res[id] = 0;
    
    	int mat1_row = idY * cols;

    	for ( int i = 0; i < cols; i++){
    		int mat2_col = i * cols2 + idX;
    		_res[id] += _mat1[mat1_row + i] * _mat2[mat2_col];
    	}
    }
}

__global__ void StandardScoreKernel(float* _mat, int rows, int cols,
	float* meanSD, float *res){
    
    int idx = blockDim.x * blockIdx.x + threadIdx.x;
    
    if (idx < cols){
    
    	for (int i = 0; i < rows; i++){
    		int idxElement = i * cols + idx;    		    						
			float _mean = meanSD[idx];
			float _sd = meanSD[cols + idx];
			
			res[idxElement] = (_mat[idxElement] - _mean) / _sd;
		}
    }
}

extern "C" {
    void g_MatMul(float* _mat1, float *_mat2, float *_res, 
		int rows1, int cols, int cols2){
    
    	float *d_mat1;
    	float *d_mat2;
    	float *d_result;
    	
    	checkCudaErrors(cudaMalloc(&d_mat1, sizeof(float) * rows1 * cols));
    	checkCudaErrors(cudaMemcpy(d_mat1, _mat1, sizeof(float) * rows1 * cols, cudaMemcpyHostToDevice));
    	
    	checkCudaErrors(cudaMalloc(&d_mat2, sizeof(float) * cols * cols2));
    	checkCudaErrors(cudaMemcpy(d_mat2, _mat2, sizeof(float) * cols * cols2, cudaMemcpyHostToDevice));
    	
    	checkCudaErrors(cudaMalloc(&d_result, sizeof(float) * rows1 * cols2));
        
       
        dim3 blockDim(N_THREADS_X, N_THREADS_Y, 1);
        dim3 gridDim( ceil((float)cols2/N_THREADS_X), ceil((float)rows1/N_THREADS_Y), 1);
        printf("gridx: %d, gridy: %d\n", gridDim.x, gridDim.y);

        AddKernel<<<gridDim, blockDim>>>(d_mat1, d_mat2, d_result, rows1, cols, cols2);
        
            
        checkCudaErrors(cudaDeviceSynchronize());
    	checkCudaErrors(cudaMemcpy(_res, d_result, sizeof(float) * rows1 * cols2, cudaMemcpyDeviceToHost));
    	checkCudaErrors(cudaFree(d_mat1));
    	checkCudaErrors(cudaFree(d_mat2));
    	checkCudaErrors(cudaFree(d_result));
    }
    
    
    void g_StandardDeviation(float* mat, int rows, int cols, 
		float* meanSD, float* result){
		
		float *d_mat;
    	float *d_meanSD;
    	float *d_result;
    	
    	checkCudaErrors(cudaMalloc(&d_mat, sizeof(float) * rows * cols));
    	checkCudaErrors(cudaMemcpy(d_mat, mat, sizeof(float) * rows * cols, cudaMemcpyHostToDevice));
    	
    	checkCudaErrors(cudaMalloc(&d_meanSD, sizeof(float) * 2 * cols));
    	checkCudaErrors(cudaMemcpy(d_meanSD, meanSD, sizeof(float) * 2 * cols, cudaMemcpyHostToDevice));
    	
    	checkCudaErrors(cudaMalloc(&d_result, sizeof(float) * rows * cols));
    	
    	dim3 blockDim(N_THREADS_X * N_THREADS_Y, 1, 1);
        dim3 gridDim(ceil((float)cols/(N_THREADS_X *  N_THREADS_Y)), 1, 1);
        
        StandardScoreKernel<<<gridDim, blockDim>>>(d_mat, rows, cols, d_meanSD, d_result);
        
        checkCudaErrors(cudaMemcpy(result, d_result, sizeof(float) * rows * cols, cudaMemcpyDeviceToHost));
    	checkCudaErrors(cudaFree(d_mat));
    	checkCudaErrors(cudaFree(d_meanSD));
    	checkCudaErrors(cudaFree(d_result));
	}
    
    
    void g_MeanSD(int rows, int cols, int depth, float *h_data, float *result, bool considerZeros){
    	//cudaSetDevice(0);

    	// Generate the keys array
    	int h_layer_keys[rows * cols];
    	for (int i = 0; i < rows; i++){
    		
    		for (int j = 0; j < cols; j++){
    			h_layer_keys[i * cols + j] = j;
    		}
    	}


    	float *d_raw, *d_val_res, *d_mean_sd, *d_sum_depths;
    	int *d_kraw, *d_keys_res, *d_nonZeros;
    	checkCudaErrors(cudaMalloc((void**) &d_raw, sizeof(float) * rows * cols));
    	checkCudaErrors(cudaMalloc((void**) &d_kraw, sizeof(int) * rows * cols));
    	checkCudaErrors(cudaMalloc((void**) &d_val_res, sizeof(float) * rows * cols));	
    	checkCudaErrors(cudaMalloc((void**) &d_keys_res, sizeof(int) * rows * cols));
    	checkCudaErrors(cudaMalloc((void**) &d_mean_sd, sizeof(float) * cols * 2));
    	checkCudaErrors(cudaMalloc((void**) &d_sum_depths, sizeof(float) * depth * cols));
    	checkCudaErrors(cudaMemset(d_val_res, 0, sizeof(float) * rows * cols));
    	checkCudaErrors(cudaMemset(d_keys_res, 0, sizeof(int) * rows * cols));
    	
    	if (!considerZeros){
    		checkCudaErrors(cudaMalloc((void**) &d_nonZeros, sizeof(int) * rows));
    		checkCudaErrors(cudaMemset(d_nonZeros, 0, sizeof(int) * rows));
    	}
    		
    		


    	thrust::device_ptr<float> dev_ptr(d_raw);
    	thrust::device_ptr<int> dev_ptr_k(d_kraw);
    	thrust::device_ptr<int> dev_ptr_k_res(d_keys_res);
    	thrust::device_ptr<float> dev_ptr_v_res(d_val_res);


    	for (int i = 0; i < depth; i++){
    		checkCudaErrors(cudaMemcpy(d_raw, &h_data[i * rows * cols], 
    				sizeof(float) * rows * cols, cudaMemcpyHostToDevice));
    		checkCudaErrors(cudaMemcpy(d_kraw, h_layer_keys, 
    				sizeof(int) * rows * cols, cudaMemcpyHostToDevice));
    		
    		// check non zeros
    		if (!considerZeros){
    			dim3 blockDim_nz(256, 1, 1);
    			dim3 gridDim_nz( ceil((float)rows/256), 1, 1);
    			K_CheckNonZerosInCol<<<gridDim_nz, blockDim_nz>>>(d_raw, rows, cols, d_nonZeros);
    		}
        	       	
    		thrust::sort_by_key(dev_ptr_k, dev_ptr_k + (rows * cols), dev_ptr);
    		cudaDeviceSynchronize();
    		thrust::reduce_by_key(dev_ptr_k, dev_ptr_k+(rows * cols), dev_ptr, dev_ptr_k_res, dev_ptr_v_res);
    		cudaDeviceSynchronize();
    		checkCudaErrors(cudaMemcpy(&d_sum_depths[i * cols], d_val_res, 
    				sizeof(float) * cols, cudaMemcpyDeviceToDevice));
    		
    	}
    	
    	float _result[cols*depth];
    	cudaMemcpy(_result, d_mean_sd, sizeof(float) * cols * depth, cudaMemcpyDeviceToHost);

    	for (int i = 0; i < cols*depth; i++)
    		fprintf(stderr, "mean: %f\n", _result[i]);

    	// Calculate the mean
    	int divide_by = rows * depth;
    	
    	if (!considerZeros){
    		thrust::device_ptr<int> dev_ptr_nonZeros(d_nonZeros);
    		divide_by = thrust::reduce(dev_ptr_nonZeros, dev_ptr_nonZeros + rows) * depth;
    		fprintf(stderr, "dividby: %d\n", divide_by);
    	}
    	cudaDeviceSynchronize();
    	
    	dim3 blockDim_m(256, 1, 1);
    	dim3 gridDim_m( ceil((float)cols/256), 1, 1);
    	K_Mean<<<gridDim_m, blockDim_m>>>(d_sum_depths, d_mean_sd, rows, cols, (float)divide_by);
    	cudaDeviceSynchronize();
    	
    	
    	/*float _result[cols];
    	cudaMemcpy(_result, d_mean_sd, sizeof(float) * cols, cudaMemcpyDeviceToHost);

    	for (int i = 0; i < cols; i++)
    		fprintf(stderr, "mean: %f\n", _result[i]);*/

    	// Variance
    	/*dim3 blockDim_v(N_THREADS_X, N_THREADS_Y);
    	dim3 gridDim_v( ceil((float)cols/N_THREADS_X),ceil((float)rows/N_THREADS_Y));

    	for (int i = 0; i < depth; i++){		
    		checkCudaErrors(cudaMemcpy(d_raw, &h_data[i * rows * cols], 
    				sizeof(float) * rows * cols, cudaMemcpyHostToDevice));
    		checkCudaErrors(cudaMemcpy(d_kraw, h_layer_keys, 
    				sizeof(int) * rows * cols, cudaMemcpyHostToDevice));
        	K_Variance<<<gridDim_v, blockDim_v>>>(d_raw, d_mean_sd, rows, cols);

    		thrust::sort_by_key(dev_ptr_k, dev_ptr_k + (rows * cols), dev_ptr);
    		thrust::reduce_by_key(dev_ptr_k, dev_ptr_k+(rows * cols), dev_ptr, dev_ptr_k_res, dev_ptr_v_res);
    		checkCudaErrors(cudaMemcpy(&d_sum_depths[i * cols], d_val_res, 
    				sizeof(float) * cols, cudaMemcpyDeviceToDevice));
    		cudaDeviceSynchronize();
    	}
    	
    	dim3 blockDim_s(256, 1, 1);
    	dim3 gridDim_s( ceil((float)cols/256), 1, 1);
    	K_StandardDeviation<<<gridDim_s, blockDim_s>>>(d_sum_depths, &d_mean_sd[cols], rows, cols, depth);
    	cudaDeviceSynchronize();*/

    	
    	checkCudaErrors(cudaMemcpy(result, d_mean_sd, 
    			sizeof(float) * cols * 2, cudaMemcpyDeviceToHost));


    	if (!considerZeros){
    		cudaFree(d_nonZeros);
    	}
    	cudaFree(d_sum_depths);
    	cudaFree(d_mean_sd);   	
    	cudaFree(d_raw);
    	cudaFree(d_kraw);
    	cudaFree(d_val_res);
    	cudaFree(d_keys_res);
    	
    	
    }
    
    
    void g_SparseMultiplication(){
    
    }
    
} 