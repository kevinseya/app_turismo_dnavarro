import { IsString, IsNumber, IsArray, IsOptional } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class CreatePostDto {
  @ApiProperty({ example: 'Cascada Azul' })
  @IsString()
  title: string;

  @ApiProperty({ example: 'Hermosa cascada en la selva.' })
  @IsString()
  description: string;

  @ApiProperty({ example: 19.4326 })
  @IsNumber()
  latitude: number;

  @ApiProperty({ example: -99.1332 })
  @IsNumber()
  longitude: number;

  @ApiProperty({ example: '+521234567890' })
  @IsString()
  phone: string;

  @ApiProperty({ example: ['uploads/img1.jpg', 'uploads/img2.jpg'], type: [String], required: false })
  @IsArray()
  @IsOptional()
  images?: string[];
}
